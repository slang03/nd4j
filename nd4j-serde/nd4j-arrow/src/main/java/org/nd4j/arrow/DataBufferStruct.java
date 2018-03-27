package org.nd4j.arrow;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Struct;
import org.nd4j.linalg.api.buffer.DataBuffer;
import org.nd4j.linalg.factory.Nd4j;

import java.nio.ByteBuffer;

public class DataBufferStruct extends Struct {

    private DataBuffer dataBuffer;

    public DataBufferStruct(DataBuffer dataBuffer) {
        this.dataBuffer = dataBuffer;
    }

    public DataBufferStruct(ByteBuffer byteBuffer,int offset) {
        __init(offset,byteBuffer);
    }

    public void __init(int _i, ByteBuffer _bb) { bb_pos = _i; bb = _bb; }
    public DataBufferStruct __assign(int _i, ByteBuffer _bb) { __init(_i, _bb); return this; }

    public static DataBuffer createFromByteBuffer(ByteBuffer bb,int bb_pos,DataBuffer.Type type,int length,int elementSize) {
        DataBuffer ret = Nd4j.createBuffer(ByteBuffer.allocateDirect(length *   elementSize),type,length,0);
        switch(type) {
            case DOUBLE:
                for(int i = 0; i < ret.length(); i++) {
                    ret.put(i,bb.getDouble(bb_pos + (i * elementSize)));
                }
                break;
            case FLOAT:
                for(int i = 0; i < ret.length(); i++) {
                    ret.put(i,bb.getFloat(bb_pos + (i * elementSize)));
                }
                break;
            case INT:
                for(int i = 0; i < ret.length(); i++) {
                    ret.put(i,bb.getInt(bb_pos + (i * elementSize)));
                }
                break;
            case LONG:
                for(int i = 0; i < ret.length(); i++) {
                    ret.put(i,bb.getLong(bb_pos + (i * elementSize)));
                }
                break;
        }

        return ret;
    }


    public static int createDataBufferStruct(FlatBufferBuilder bufferBuilder,DataBuffer create) {
        //bufferBuilder.prep(create.getElementSize(), (int) create.length());
        for(int i = (int) (create.length() - 1); i >= 0; i--) {
            switch(create.dataType()) {
                case DOUBLE:
                    bufferBuilder.putDouble(create.getDouble(i));
                    break;
                case FLOAT:
                    bufferBuilder.putFloat(create.getFloat(i));
                    break;
                case INT:
                    bufferBuilder.putInt(create.getInt(i));
                    break;
                case LONG:
                    bufferBuilder.putLong(create.getLong(i));
            }
        }

        return bufferBuilder.offset();

    }
}
