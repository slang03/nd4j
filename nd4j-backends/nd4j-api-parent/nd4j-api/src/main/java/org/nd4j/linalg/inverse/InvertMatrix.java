package org.nd4j.linalg.inverse;

import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.SingularMatrixException;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.checkutil.CheckUtil;

/**
 * Created by agibsoncccc on 11/30/15.
 */
public class InvertMatrix {


    /**
     * Inverts a matrix
     * @param arr the array to invert
     * @param inPlace Whether to store the result in {@code arr}
     * @return the inverted matrix
     */
    public static INDArray invert(INDArray arr, boolean inPlace) {
        if (!arr.isSquare()) {
            throw new IllegalArgumentException("invalid array: must be square matrix");
        }

        //FIX ME: Please
       /* int[] IPIV = new int[arr.length() + 1];
        int LWORK = arr.length() * arr.length();
        INDArray WORK = Nd4j.create(new double[LWORK]);
        INDArray inverse = inPlace ? arr : arr.dup();
        Nd4j.getBlasWrapper().lapack().getrf(arr);
        Nd4j.getBlasWrapper().lapack().getri(arr.size(0),inverse,arr.size(0),IPIV,WORK,LWORK,0);*/

        RealMatrix rm = CheckUtil.convertToApacheMatrix(arr);
        RealMatrix rmInverse = new LUDecomposition(rm).getSolver().getInverse();


        INDArray inverse = CheckUtil.convertFromApacheMatrix(rmInverse);
        if (inPlace)
            arr.assign(inverse);
        return inverse;

    }

    /**
     * Compute the left pseudo inverse. Input matrix must have full column rank.
     *
     * See also: <a href="https://en.wikipedia.org/wiki/Moore%E2%80%93Penrose_inverse#Definition">Moore–Penrose inverse</a>
     *
     * @param arr Input matrix
     * @param inPlace Whether to store the result in {@code arr}
     * @return Left pseudo inverse of {@code arr}
     * @exception IllegalArgumentException Input matrix {@code arr} did not have full column rank.
     */
    public static INDArray pLeftInvert(INDArray arr, boolean inPlace) {
        try {
          final INDArray inv = invert(arr.transpose().mmul(arr), inPlace).mmul(arr.transpose());
          if (inPlace) arr.assign(inv);
          return inv;
        } catch (SingularMatrixException e) {
          throw new IllegalArgumentException(
              "Full column rank condition for left pseudo inverse was not met.");
        }
    }

    /**
     * Compute the right pseudo inverse. Input matrix must have full row rank.
     *
     * See also: <a href="https://en.wikipedia.org/wiki/Moore%E2%80%93Penrose_inverse#Definition">Moore–Penrose inverse</a>
     *
     * @param arr Input matrix
     * @param inPlace Whether to store the result in {@code arr}
     * @return Right pseudo inverse of {@code arr}
     * @exception IllegalArgumentException Input matrix {@code arr} did not have full row rank.
     */
    public static INDArray pRightInvert(INDArray arr, boolean inPlace) {
        try{
            final INDArray inv = arr.transpose().mmul(invert(arr.mmul(arr.transpose()), inPlace));
            if (inPlace) arr.assign(inv);
            return inv;
        } catch (SingularMatrixException e){
            throw new IllegalArgumentException(
                "Full row rank condition for right pseudo inverse was not met.");
        }
    }
}
