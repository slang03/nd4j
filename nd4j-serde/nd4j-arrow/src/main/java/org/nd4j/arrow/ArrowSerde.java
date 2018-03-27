package org.nd4j.arrow;

import com.google.flatbuffers.FlatBufferBuilder;
import org.apache.arrow.flatbuf.*;
import org.nd4j.linalg.api.buffer.DataBuffer;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.exception.ND4JIllegalStateException;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.util.ArrayUtil;

/**
 * Conversion to and from arrow {@link Tensor}
 * and {@link INDArray}
 *
 * @author Adam Gibson
 */
public class ArrowSerde {


    /**
     * Convert a {@link Tensor}
     * to an {@link INDArray}
     * @param tensor the input tensor
     * @return the equivalent {@link INDArray}
     */
    public static INDArray fromTensor(Tensor tensor) {
        byte b = tensor.typeType();
        int[] shape = new int[tensor.shapeLength()];
        int[] stride = new int[tensor.stridesLength()];
        for(int i = 0; i < shape.length; i++) {
            shape[i] = (int) tensor.shape(i).size();
            stride[i] = (int) tensor.strides(i);
        }

        int length = ArrayUtil.prod(shape);
        Buffer buffer = tensor.data();
        if(buffer == null) {
            throw new ND4JIllegalStateException("Buffer was not serialized properly.");
        }
        //deduce element size
        int elementSize = (int) buffer.length() / length;
        DataBuffer.Type  type = typeFromTensorType(b,elementSize);
        DataBuffer dataBuffer = DataBufferStruct.createFromByteBuffer(tensor.getByteBuffer(),(int) tensor.data().offset(),type,length,elementSize);
        INDArray arr = Nd4j.create(dataBuffer,shape);
        arr.setShapeAndStride(shape,stride);
        return arr;
    }

    /**
     * Convert an {@link INDArray}
     * to an arrow {@link Tensor}
     * @param arr the array to convert
     * @return the equivalent {@link Tensor}
     */
    public static Tensor toTensor(INDArray arr) {
        FlatBufferBuilder bufferBuilder = new FlatBufferBuilder(1024);
        long[] strides = getArrowStrides(arr);
        int shapeOffset = createDims(bufferBuilder,arr);
        int stridesOffset = Tensor.createStridesVector(bufferBuilder,strides);

        Tensor.startTensor(bufferBuilder);

        addTypeTypeRelativeToNDArray(bufferBuilder,arr);
        Tensor.addShape(bufferBuilder,shapeOffset);
        Tensor.addStrides(bufferBuilder,stridesOffset);

        Tensor.addData(bufferBuilder,addDataForArr(bufferBuilder,arr));
        int endTensor = Tensor.endTensor(bufferBuilder);
        Tensor.finishTensorBuffer(bufferBuilder,endTensor);
        return Tensor.getRootAsTensor(bufferBuilder.dataBuffer());
    }

    public static int createBufferVector(FlatBufferBuilder builder, byte[] data) { builder.startVector(1, data.length, 1); for (int i = data.length - 1; i >= 0; i--) builder.addByte(data[i]); return builder.endVector(); }



    public static int addDataForArr(FlatBufferBuilder bufferBuilder, INDArray arr) {
        int offset = DataBufferStruct.createDataBufferStruct(bufferBuilder,arr.data());
        int ret = Buffer.createBuffer(bufferBuilder,offset,arr.data().length() * arr.data().getElementSize());
        return ret;

    }

    public static void addTypeTypeRelativeToNDArray(FlatBufferBuilder bufferBuilder,INDArray arr) {
        switch(arr.data().dataType()) {
            case LONG:
            case INT:
                Tensor.addTypeType(bufferBuilder,Type.Int);
                break;
            case FLOAT:
            case DOUBLE:
                Tensor.addTypeType(bufferBuilder,Type.FloatingPoint);
                break;
        }
    }

    public static int createDims(FlatBufferBuilder bufferBuilder,INDArray arr) {
        int[] tensorDimOffsets = new int[arr.rank()];
        int[] nameOffset = new int[arr.rank()];
        for(int i = 0; i < tensorDimOffsets.length; i++) {
            nameOffset[i] = bufferBuilder.createString("");
            tensorDimOffsets[i] = TensorDim.createTensorDim(bufferBuilder,arr.size(i),nameOffset[i]);
        }

        return Tensor.createShapeVector(bufferBuilder,tensorDimOffsets);
    }


    public static int createTypeRelativeToNDArray(FlatBufferBuilder bufferBuilder,INDArray arr) {
        int ret;
        switch (arr.data().dataType()) {
            case FLOAT:
                ret = FloatingPoint.createFloatingPoint(bufferBuilder, Precision.SINGLE);
                break;
            case DOUBLE:
                ret = FloatingPoint.createFloatingPoint(bufferBuilder, Precision.DOUBLE);
                break;
            case HALF:
                ret = FloatingPoint.createFloatingPoint(bufferBuilder,Precision.HALF);
                break;
            case INT:
                ret = Int.createInt(bufferBuilder,32,true);
                break;
            case LONG:
                ret = Int.createInt(bufferBuilder,64,true);
                break;
            default:
                throw new IllegalArgumentException("Illegal type " + arr.data().dataType());
        }

        return ret;
    }


    public static long[] getArrowStrides(INDArray arr) {
        long[] ret = new long[arr.rank()];
        for(int i = 0; i < arr.rank(); i++) {
            ret[i] = arr.stride(i) * arr.data().getElementSize();
        }

        return ret;
    }

    public static int[] getNd4jStrides(long[] arrowStrides,int elementSize) {
        int[] ret = new int[arrowStrides.length];
        for(int i = 0; i < arrowStrides.length; i++) {
            ret[i] = (int) arrowStrides[i] / elementSize;
        }

        return ret;
    }

    public static byte tensorTypeFrom(INDArray arr) {
        if(arr.data().dataType() == DataBuffer.Type.FLOAT || arr.data().dataType() == DataBuffer.Type.DOUBLE)
            return Type.Decimal;
        else if(arr.data().dataType() == DataBuffer.Type.INT || arr.data().dataType() == DataBuffer.Type.LONG)
            return Type.Int;
        throw new IllegalArgumentException("Illegal data type " + arr.data().dataType());
    }

    public static DataBuffer.Type typeFromTensorType(byte type,int elementSize) {
        if(type == Type.Decimal || type == Type.FloatingPoint) {
            if(elementSize == 4) {
                return DataBuffer.Type.FLOAT;
            }
            else if(elementSize == 8) {
                return DataBuffer.Type.DOUBLE;
            }
        }
        else if(type == Type.Int) {
            if(elementSize == 4) {
                return DataBuffer.Type.INT;
            }
            else if(elementSize == 8) {
                return DataBuffer.Type.LONG;
            }
        }
        else {
            throw new IllegalArgumentException("Only valid types are Type.Decimal and Type.Int");
        }

        throw new IllegalArgumentException("Unable to determine data type");
    }
}
