package buildcraft.test.lib.misc;

import org.junit.Assert;
import org.junit.Test;

import net.minecraft.network.PacketBuffer;

import buildcraft.lib.misc.MessageUtil;

import io.netty.buffer.Unpooled;

public class MessageUtilTester {
    @Test
    public void testBooleanArraySmall() {
        boolean[] expected = { false, true, false };

        PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());
        MessageUtil.writeBooleanArray(buffer, expected);
        boolean[] got = MessageUtil.readBooleanArray(buffer, expected.length);
        Assert.assertArrayEquals(expected, got);
    }

    @Test
    public void testBooleanArrayLarge() {
        boolean[] expected = { false, true, false, false, false, true, true, true, true, true, true, true, false };

        PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());
        MessageUtil.writeBooleanArray(buffer, expected);
        boolean[] got = MessageUtil.readBooleanArray(buffer, expected.length);
        Assert.assertArrayEquals(expected, got);
    }

    @Test
    public void testBooleanArrayLargeManual() {
        boolean[] expected = { false, true, false, false, false, true, true, true, true, true, true, true, false };

        PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());
        MessageUtil.writeBooleanArray(buffer, expected);

        boolean[] got = new boolean[expected.length];
        MessageUtil.readBooleanArray(buffer, got);
        Assert.assertArrayEquals(expected, got);
    }
}
