package buildcraft.lib.nbt;

import java.io.IOException;

import io.netty.buffer.ByteBuf;

public enum WrittenType {
    BYTE(1, Byte.MAX_VALUE),
    SHORT(2, Short.MAX_VALUE),
    MEDIUM(3, (1 << 24) - 1),
    INT(4, Integer.MAX_VALUE);

    public static final WrittenType[] ORDERED_VALUES = { BYTE, SHORT, MEDIUM, INT };

    public final int numBytes;
    private final int maxStorableValue;

    private WrittenType(int numBytes, int maxStorableValue) {
        this.maxStorableValue = maxStorableValue;
        this.numBytes = numBytes;
    }

    public static WrittenType getForSize(int size) {
        for (WrittenType type : ORDERED_VALUES) {
            if (size < type.maxStorableValue) {
                return type;
            }
        }
        throw new IllegalArgumentException("Waaaaay too big index list (" + size + ")");
    }

    public void writeType(ByteBuf bytes) {
        bytes.writeByte(numBytes - 1);
    }

    public static WrittenType readType(ByteBuf bytes) throws IOException {
        byte val = bytes.readByte();
        for (WrittenType type : ORDERED_VALUES) {
            if (val == type.numBytes - 1) {
                return type;
            }
        }
        throw new IOException("Incorrect size given, expected any of [0, 1, 2, 3] but got " + val);
    }

    public void writeIndex(ByteBuf bytes, int index) {
        switch (this) {
            case BYTE: {
                bytes.writeByte(index);
                break;
            }
            case SHORT: {
                bytes.writeShort(index);
                break;
            }
            case MEDIUM: {
                bytes.writeMedium(index);
                break;
            }
            default:
            case INT: {
                bytes.writeInt(index);
                break;
            }
        }
    }

    public int readIndex(ByteBuf bytes) {
        switch (this) {
            case BYTE:
                return bytes.readUnsignedByte();
            case SHORT:
                return bytes.readUnsignedShort();
            case MEDIUM:
                return bytes.readUnsignedMedium();
            default:
            case INT:
                return bytes.readInt();
        }
    }
}
