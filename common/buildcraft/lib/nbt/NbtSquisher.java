package buildcraft.lib.nbt;

import java.io.IOException;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * 
 */
public class NbtSquisher {
    /* Defines a compression program that can turn large, mostly-similar, dense, NBTTagCompounds into much smaller
     * variants.
     * 
     * Compression has the following steps:
     * 
     * - 1: */

    public static byte[] squish(NBTTagCompound nbt) {
        NBTSquishMap map = new NBTSquishMap();
        map.addTag(nbt);
        ByteBuf buf = Unpooled.buffer();
        if (NBTSquishDebugging.debug) {
            buf = new PrintingByteBuf(buf);
        }

        WrittenType type = map.write(buf);
        type.writeIndex(buf, map.indexOfTag(nbt));
        byte[] bytes = new byte[buf.readableBytes()];
        buf.readBytes(bytes);

        NBTSquishDebugging.log(() -> "\nUsed type " + type + " (as there are " + map.size() + " object types)");
        return bytes;
    }

    public static NBTTagCompound expand(byte[] bytes) throws IOException, IndexOutOfBoundsException {
        ByteBuf buf = Unpooled.copiedBuffer(bytes);
        NBTSquishMap map = new NBTSquishMap(buf);
        WrittenType type = map.getWrittenType();
        int index = type.readIndex(buf);
        NBTBase nbt = map.getTagAt(index);
        return (NBTTagCompound) nbt;
    }

    private static final class PrintingByteBuf extends PacketBuffer {
        private PrintingByteBuf(ByteBuf wrapped) {
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
}
