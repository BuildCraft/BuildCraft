package buildcraft.lib.nbt;

import net.minecraft.network.PacketBuffer;

import io.netty.buffer.ByteBuf;

public final class PrintingByteBuf extends PacketBuffer {
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
        return writeBytes(src, src.readableBytes());
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
}
