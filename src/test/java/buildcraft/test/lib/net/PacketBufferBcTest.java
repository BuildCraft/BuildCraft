package buildcraft.test.lib.net;

import org.junit.Assert;
import org.junit.Test;

import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.EnumFacing;

import buildcraft.lib.nbt.PrintingByteBuf;
import buildcraft.lib.net.PacketBufferBC;

import io.netty.buffer.Unpooled;

public class PacketBufferBcTest {
    @Test
    public void testAmongst() {
        PacketBufferBC buffer = new PacketBufferBC(Unpooled.buffer());

        buffer.writeInt(49);
        buffer.writeBoolean(true);

        buffer.writeShort(95);
        buffer.writeBoolean(false);

        buffer.writeByte(11);
        buffer.writeBoolean(true);

        byte[] expected = {
            // writeInt(49)
            0, 0, 0, 49,
            // flag(true, false, true)
            1 + 4,
            // writeShort(95)
            0, 95,
            // writeByte(11)
            11

        };

        byte[] read = new byte[expected.length];
        buffer.getBytes(0, read);
        Assert.assertArrayEquals(expected, read);

        Assert.assertEquals(49, buffer.readInt());
        Assert.assertTrue(buffer.readBoolean());

        Assert.assertEquals(95, buffer.readShort());
        Assert.assertFalse(buffer.readBoolean());

        Assert.assertEquals(11, buffer.readByte());
        Assert.assertTrue(buffer.readBoolean());
    }

    @Test
    public void testMultiple() {
        PacketBufferBC buffer = new PacketBufferBC(Unpooled.buffer());

        for (int i = 0; i < 17; i++) {
            boolean value = i % 2 == 0;
            buffer.writeBoolean(value);
        }

        PrintingByteBuf print = new PrintingByteBuf(Unpooled.buffer());
        print.writeBytes(buffer, 0, buffer.readableBytes());

        Assert.assertEquals(3, buffer.writerIndex());

        for (int i = 0; i < 17; i++) {
            boolean value = i % 2 == 0;
            Assert.assertTrue(value == buffer.readBoolean());
        }

        Assert.assertEquals(3, buffer.readerIndex());
    }

    @Test
    public void testFixedBits() {
        PacketBufferBC buffer = new PacketBufferBC(new PrintingByteBuf(Unpooled.buffer()));
        int value = 0xA4;
        int value2 = 1;
        int value3 = 0xF_81_67;
        int value4 = 0x7E_DC_A9_87;

        buffer.writeFixedBits(value, 10);
        buffer.writeFixedBits(value2, 2);
        buffer.writeBoolean(true);
        buffer.writeFixedBits(value3, 20);
        buffer.writeFixedBits(value4, 31);

        int read = buffer.readFixedBits(10);
        Assert.assertEquals(value, read);

        int read2 = buffer.readFixedBits(2);
        Assert.assertEquals(value2, read2);

        Assert.assertTrue(buffer.readBoolean());

        int read3 = buffer.readFixedBits(20);
        Assert.assertEquals(value3, read3);

        int read4 = buffer.readFixedBits(31);
        Assert.assertEquals(value4, read4);

        Assert.assertEquals(8, buffer.readerIndex());
        Assert.assertEquals(8, buffer.writerIndex());
    }

    @Test
    public void testEnums() {
        PacketBufferBC buffer = new PacketBufferBC(new PrintingByteBuf(Unpooled.buffer()));

        buffer.writeBoolean(true);
        buffer.writeEnumValue(EnumFacing.DOWN);
        buffer.writeEnumValue(EnumFacing.SOUTH);
        buffer.writeEnumValue(EnumDyeColor.BROWN);
        buffer.writeEnumValue(EnumDyeColor.CYAN);

        Assert.assertTrue(buffer.readBoolean());
        Assert.assertEquals(EnumFacing.DOWN, buffer.readEnumValue(EnumFacing.class));
        Assert.assertEquals(EnumFacing.SOUTH, buffer.readEnumValue(EnumFacing.class));
        Assert.assertEquals(EnumDyeColor.BROWN, buffer.readEnumValue(EnumDyeColor.class));
        Assert.assertEquals(EnumDyeColor.CYAN, buffer.readEnumValue(EnumDyeColor.class));

        Assert.assertEquals(2, buffer.readerIndex());
        Assert.assertEquals(2, buffer.writerIndex());
    }
}
