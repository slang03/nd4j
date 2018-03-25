package org.nd4j.arrow;

import com.google.flatbuffers.FlatBufferBuilder;
import org.apache.arrow.flatbuf.Tensor;
import org.apache.arrow.flatbuf.Type;
import org.nd4j.linalg.api.buffer.DataBuffer;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.util.ArrayUtil;

import java.nio.ByteBuffer;

/**
 * Conversion to and from arrow {@link Tensor}
 * and {@link INDArray}
 *
 * @author Adam Gibson
 */
public class ArrowSerde {


    public final static int TYPE_TYPE_OFFSET = 4;
    public final static int STRIDES_OFFSET = 10;
    public final static int SHAPES_OFFSET = 8;
    public final static int DATA_OFFSET = 12;

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
        int length = ArrayUtil.prod(shape);
        for(int i = 0; i < shape.length; i++) {
            shape[i] = (int) tensor.shape(i).size();
            stride[i] = (int) tensor.strides(i);
        }

        //deduce element size
        int elementSize = (int) tensor.data().length() / length;
        DataBuffer.Type  type = typeFromTensorType(b,elementSize);
        ByteBuffer data = (ByteBuffer) tensor.getByteBuffer().position((int) tensor.data().offset());
        DataBuffer dataBuffer = Nd4j.createBuffer(data,type,length);
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
        FlatBufferBuilder bufferBuilder = new FlatBufferBuilder(1024);;

        bufferBuilder.addInt(tensorTypeFrom(arr));
        int typeOffset = bufferBuilder.offset();

        int vectorEnd = 0;
        if(arr.data().dataType() == DataBuffer.Type.DOUBLE) {
            bufferBuilder.startVector(8, (int) arr.data().length(), 4);
            for(int i = 0; i < arr.data().length(); i++) {
                bufferBuilder.addDouble(arr.data().getDouble(i));
            }

            vectorEnd = bufferBuilder.endVector();
        }
        else if(arr.data().dataType() == DataBuffer.Type.FLOAT) {
            bufferBuilder.startVector(4, (int) arr.data().length(), 4);
            for(int i = 0; i < arr.data().length(); i++) {
                bufferBuilder.addFloat(arr.data().getFloat(i));
            }

            vectorEnd = bufferBuilder.endVector();
        }
        else if(arr.data().dataType() == DataBuffer.Type.INT) {
            bufferBuilder.startVector(4, (int) arr.data().length(), 0);
            for(int i = 0; i < arr.data().length(); i++) {
                bufferBuilder.addInt(arr.data().getInt(i));
            }

            vectorEnd = bufferBuilder.endVector();

        }


        long[] strides = getArrowStrides(arr);
        int shapeOffset = Tensor.createShapeVector(bufferBuilder,arr.shape());
        int stridesOffset = Tensor.createStridesVector(bufferBuilder,strides);


        Tensor.startTensor(bufferBuilder);
        Tensor.addType(bufferBuilder,typeOffset);
        Tensor.addShape(bufferBuilder,shapeOffset);
        Tensor.addStrides(bufferBuilder,stridesOffset);
        Tensor.finishTensorBuffer(bufferBuilder,vectorEnd);
        Tensor.endTensor(bufferBuilder);
        ByteBuffer buffer = bufferBuilder.dataBuffer();
        buffer.rewind();
        return Tensor.getRootAsTensor(buffer);
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
        if(type == Type.Decimal) {
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
