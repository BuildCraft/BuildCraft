package buildcraft.test.lib.net;

import io.netty.buffer.Unpooled;

import net.minecraft.util.Direction;
import net.minecraft.item.DyeColor;
import org.junit.Assert;
import org.junit.Test;

import buildcraft.lib.net.PacketBufferBC;

public class PacketBufferBcTest {
    @Test
    public void testAmongst() {
        PacketBufferBC buffer = new PrintingByteBuf(Unpooled.buffer());

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
        PacketBufferBC buffer = new PrintingByteBuf(Unpooled.buffer());

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
        PacketBufferBC buffer = new PrintingByteBuf(Unpooled.buffer());
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
        PacketBufferBC buffer = new PrintingByteBuf(Unpooled.buffer());

        buffer.writeBoolean(true);
        buffer.writeEnum(Direction.DOWN);
        buffer.writeEnum(Direction.SOUTH);
        buffer.writeEnum(DyeColor.BROWN);
        buffer.writeEnum(DyeColor.CYAN);

        Assert.assertTrue(buffer.readBoolean());
        Assert.assertEquals(Direction.DOWN, buffer.readEnum(Direction.class));
        Assert.assertEquals(Direction.SOUTH, buffer.readEnum(Direction.class));
        Assert.assertEquals(DyeColor.BROWN, buffer.readEnum(DyeColor.class));
        Assert.assertEquals(DyeColor.CYAN, buffer.readEnum(DyeColor.class));

        Assert.assertEquals(2, buffer.readerIndex());
        Assert.assertEquals(2, buffer.writerIndex());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidEnum_0_read() {
        PacketBufferBC buffer = new PrintingByteBuf(Unpooled.buffer());
        /* Attempting to write out an enum value that doesn't have any values is definitely a bug */
        buffer.readEnum(Enum_0.class);
    }

    @Test
    public void testSizedEnums() {
        PacketBufferBC buffer = new PrintingByteBuf(Unpooled.buffer());

        buffer.writeEnum(Enum_1.A);

        buffer.writeEnum(Enum_2.A);
        buffer.writeEnum(Enum_2.B);

        //@formatter:off
        for (Enum_3 e : Enum_3.values()) buffer.writeEnum(e);
        for (Enum_4 e : Enum_4.values()) buffer.writeEnum(e);
        for (Enum_5 e : Enum_5.values()) buffer.writeEnum(e);
        for (Enum_6 e : Enum_6.values()) buffer.writeEnum(e);
        for (Enum_7 e : Enum_7.values()) buffer.writeEnum(e);
        for (Enum_8 e : Enum_8.values()) buffer.writeEnum(e);
        for (Enum_9 e : Enum_9.values()) buffer.writeEnum(e);
        //@formatter:on

        Assert.assertEquals(Enum_1.A, buffer.readEnum(Enum_1.class));

        Assert.assertEquals(Enum_2.A, buffer.readEnum(Enum_2.class));
        Assert.assertEquals(Enum_2.B, buffer.readEnum(Enum_2.class));

        //@formatter:off
        for (Enum_3 e : Enum_3.values()) Assert.assertEquals(e, buffer.readEnum(Enum_3.class));
        for (Enum_4 e : Enum_4.values()) Assert.assertEquals(e, buffer.readEnum(Enum_4.class));
        for (Enum_5 e : Enum_5.values()) Assert.assertEquals(e, buffer.readEnum(Enum_5.class));
        for (Enum_6 e : Enum_6.values()) Assert.assertEquals(e, buffer.readEnum(Enum_6.class));
        for (Enum_7 e : Enum_7.values()) Assert.assertEquals(e, buffer.readEnum(Enum_7.class));
        for (Enum_8 e : Enum_8.values()) Assert.assertEquals(e, buffer.readEnum(Enum_8.class));
        for (Enum_9 e : Enum_9.values()) Assert.assertEquals(e, buffer.readEnum(Enum_9.class));
        //@formatter:on
    }

    //@formatter:off
    public enum Enum_0 {}
    public enum Enum_1 {A}
    public enum Enum_2 {A, B}
    public enum Enum_3 {A, B, C}
    public enum Enum_4 {A, B, C, D}
    public enum Enum_5 {A, B, C, D, E}
    public enum Enum_6 {A, B, C, D, E, F}
    public enum Enum_7 {A, B, C, D, E, F, G}
    public enum Enum_8 {A, B, C, D, E, F, G, H}
    public enum Enum_9 {A, B, C, D, E, F, G, H, I}
    //@formatter:on
}
