package org.nd4j.linalg.string;

import org.apache.commons.lang3.StringUtils;
import org.nd4j.linalg.api.complex.IComplexNDArray;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.ops.transforms.Transforms;

import java.text.DecimalFormat;

/**
 * @author Adam Gibson
 * @author Susan Eraly
 */
public class NDArrayStrings {

    private String colSep = ",";
    private String newLineSep = ",";
    private int padding = 7;
    private int precision = 2;
    private DecimalFormat decimalFormat = new DecimalFormat("##0.####");
    private boolean dontOverrideFormat = false;

    public NDArrayStrings() {
        this(",", 4);
    }

    public NDArrayStrings(String colSep) {
        this(colSep, 4);
    }

    public NDArrayStrings(int precision) {
        this(",", precision);
    }

    /**
     * Specify a delimiter for elements in columns for 2d arrays (or in the rank-1th dimension in higher order arrays)
     * Note that separator in elements in remaining dimensions defaults to ",\n"
     *
     * @param colSep    field separating columns;
     * @param precision
     */
    public NDArrayStrings(String colSep, int precision) {
        this.colSep = colSep;
        if (!colSep.replaceAll("\\s", "").equals(",")) this.newLineSep = "";
        this.precision = precision;
        String decFormatNum = "##0.";
        while (precision > 0) {
            decFormatNum += "0";
            precision -= 1;
        }
        this.decimalFormat = new DecimalFormat(decFormatNum);
    }

    public NDArrayStrings(String colSep, String decFormat) {
        this.colSep = colSep;
        this.decimalFormat = new DecimalFormat(decFormat);
        if (decFormat.toUpperCase().contains("E")) {
            this.padding = decFormat.length() + 3;
        } else {
            this.padding = decFormat.length() + 1;
        }
        this.dontOverrideFormat = true;
    }

    public String format(INDArray arr) {
        return format(arr, true);
    }

    /**
     * Format the given ndarray as a string
     *
     * @param arr       the array to format
     * @param summarize If true the number of elements in the array is greater than > 1000 only the first three and last elements in any dimension will print
     * @return the formatted array
     */
    public String format(INDArray arr, boolean summarize) {
        double minAbsValue = Transforms.abs(arr).minNumber().doubleValue();
        double maxAbsValue = Transforms.abs(arr).maxNumber().doubleValue();
        if (!dontOverrideFormat) {
            if ((minAbsValue <= 0.0001) || (maxAbsValue / minAbsValue) > 1000 || (maxAbsValue > 1000)) {
                String decFormatNum = "0.";
                while (this.precision > 0) {
                    decFormatNum += "0";
                    precision -= 1;
                }
                this.decimalFormat = new DecimalFormat(decFormatNum + "E0");
                this.padding = decFormatNum.length() + 5; //E00? and sign for mantissa and exp
            } else {
                if (maxAbsValue < 10) {
                    this.padding = this.precision + 3;
                } else if (maxAbsValue < 100) {
                    this.padding = this.precision + 4;
                } else {
                    this.padding = this.precision + 5;
                }
            }
        }
        if (summarize && arr.length() > 10) return format(arr, 0, true, false);
        return format(arr, 0, false, false);
    }

    private String format(INDArray arr, int offset, boolean summarize, boolean bypass) {
        if (bypass) return "";
        int rank = arr.rank();
        if (arr.isScalar() && rank == 0) {
            //true scalar i.e shape = [] not legacy which is [1,1]
            if (arr instanceof IComplexNDArray) {
                return ((IComplexNDArray) arr).getComplex(0).toString();
            }
            return decimalFormat.format(arr.getDouble(0));
        } else if (rank == 1) {
            //true vector
            return vectorToString(arr, summarize);
        } else if (arr.isRowVector()) {
            //a slice from a higher dim array
            if (offset == 0) {
                StringBuilder sb = new StringBuilder();
                sb.append("[");
                sb.append(vectorToString(arr, summarize));
                sb.append("]");
                return sb.toString();
            }
            return vectorToString(arr, summarize);
        } else {
            offset++;
            StringBuilder sb = new StringBuilder();
            sb.append("[");
            for (int i = 0; i < arr.slices(); i++) {
                if (summarize && i > 2 && i < arr.length() - 3) {
                    if (i == 3) sb.append("  ...");
                } else {
                    if (arr.rank() == 3 && arr.slice(i).isRowVector()) sb.append("[");
                    //hack fix for slice issue with 'f' order
                    if (arr.ordering() == 'f' && arr.rank() > 2 && arr.size(arr.rank() - 1) == 1) {
                        sb.append(format(arr.dup('c').slice(i), offset, summarize, false));
                    } else {
                        sb.append(format(arr.slice(i), offset, summarize, false));
                    }
                    if (i != arr.slices() - 1) {
                        if (arr.rank() == 3 && arr.slice(i).isRowVector()) sb.append("]");
                        sb.append(newLineSep + " \n");
                        sb.append(StringUtils.repeat("\n", rank - 2));
                        sb.append(StringUtils.repeat(" ", offset));
                    } else {
                        if (arr.rank() == 3 && arr.slice(i).isRowVector()) sb.append("]");
                    }
                }
            }
            sb.append("]");
            return sb.toString();
        }
    }

    private String vectorToString(INDArray arr, boolean summarize) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < arr.length(); i++) {
            if (arr instanceof IComplexNDArray) {
                sb.append(((IComplexNDArray) arr).getComplex(i).toString());
            } else {
                if (summarize && i > 2 && i < arr.length() - 3) {
                    if (i == 3) sb.append("  ...");
                } else {
                    sb.append(String.format("%1$" + padding + "s", decimalFormat.format(arr.getDouble(i))));
                }
            }
            if (i < arr.length() - 1) {
                if (!summarize || i < 2 || i > arr.length() - 3) {
                    sb.append(colSep);
                }
            }
        }
        sb.append("]");
        return sb.toString();
    }

}
