package buildcraft.lib.nbt;

import buildcraft.lib.net.PacketBufferBC;

import io.netty.buffer.ByteBuf;

public final class PrintingByteBuf extends PacketBufferBC {
    public PrintingByteBuf(ByteBuf wrapped) {
        super(wrapped);
    }

    @Override
    public ByteBuf writeByte(int val) {
        System.out.print(padLength(2, val));
        super.writeByte(val);
        return this;
    }

    @Override
    public ByteBuf writeBytes(byte[] val) {
        for (byte b : val) {
            writeByte(Byte.toUnsignedInt(b));
        }
        return this;
    }

    @Override
    public ByteBuf writeBytes(byte[] src, int srcIndex, int length) {
        for (int i = 0; i < length; i++) {
            writeByte(src[i + srcIndex]);
        }
        return this;
    }

    @Override
    public ByteBuf writeBytes(ByteBuf src) {
        writeBytes(src, src.readableBytes());
        return this;
    }

    @Override
    public ByteBuf writeBytes(ByteBuf src, int length) {
        for (int i = 0; i < length; i++) {
            writeByte(src.readByte());
        }
        return this;
    }

    @Override
    public ByteBuf writeBytes(ByteBuf src, int srcIndex, int length) {
        for (int i = 0; i < length; i++) {
            writeByte(src.getByte(i + srcIndex));
        }
        return this;
    }

    @Override
    public ByteBuf writeShort(int val) {
        System.out.print(padLength(4, val));
        super.writeShort(val);
        return this;
    }

    @Override
    public ByteBuf writeInt(int val) {
        System.out.print(padLength(8, val));
        super.writeInt(val);
        return this;
    }

    @Override
    public ByteBuf writeLong(long val) {
        System.out.print(padLength(16, val));
        super.writeLong(val);
        return this;
    }

    @Override
    public ByteBuf writeFloat(float val) {
        System.out.print(padLength(8, Float.floatToRawIntBits(val)));
        super.writeFloat(val);
        return this;
    }

    @Override
    public ByteBuf writeDouble(double val) {
        System.out.print(padLength(16, Double.doubleToRawLongBits(val)));
        super.writeDouble(val);
        return this;
    }

    @Override
    public ByteBuf setByte(int index, int value) {
        System.out.println("\n  Set " + index + " (" + new String(padLength(2, getByte(index)))//
            + " ) to" + new String(padLength(2, value)));
        super.setByte(index, value);
        return this;
    }

    private static char[] padLength(int length, long val) {
        String s = Long.toHexString(val);
        char[] chars = new char[length + 1];
        chars[0] = ' ';
        int diff = length - s.length();
        for (int i = 0; i < diff; i++) {
            chars[i + 1] = '0';
        }
        for (int i = 0; i < s.length(); i++) {
            chars[i + diff + 1] = s.charAt(i);
        }
        return chars;
    }

    // PacketBufferBC overrides

    @Override
    public PacketBufferBC writeFixedBits(int value, int length) throws IllegalArgumentException {
        System.out.println("Writing " + length + " fixed bits ( " + new String(padLength(length, value)) + " )");
        super.writeFixedBits(value, length);
        return this;
    }

    @Override
    public PacketBufferBC writeEnumValue(Enum<?> value) {
        System.out.println("Writing " + value + " from " + value.getClass());
        super.writeEnumValue(value);
        return this;
    }
}
