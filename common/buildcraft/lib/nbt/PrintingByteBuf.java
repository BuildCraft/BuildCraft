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
        System.out.print(padLength(8, val));
        super.writeFloat(val);
        return this;
    }

    @Override
    public ByteBuf writeDouble(double val) {
        System.out.print(padLength(16, val));
        super.writeDouble(val);
        return this;
    }

    private static String padLength(int length, long val) {
        String s = Long.toHexString(val);
        while (s.length() < length) {
            s = "0" + s;
        }
        return " " + s;
    }

    private static String padLength(int length, double val) {
        return padLength(length, length == 8 ? Float.floatToRawIntBits((float) val) : Double.doubleToRawLongBits(val));
    }
}
