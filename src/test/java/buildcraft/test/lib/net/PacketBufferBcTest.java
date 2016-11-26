package buildcraft.test.lib.net;

import org.junit.Assert;
import org.junit.Test;

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
}
