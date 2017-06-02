package buildcraft.test.lib.misc;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

import buildcraft.lib.misc.HashUtil;
import buildcraft.lib.nbt.NbtSquisher;

import buildcraft.test.lib.nbt.NbtSquisherTester;

public class HashUtilTester {
    private static final byte[] HASH = { 0, 1, 5, 9, (byte) 0xff, (byte) 0xbc };
    private static final String STR = "00010509ffbc";

    private static final byte[] DATA = NbtSquisher.squishVanillaUncompressed(NbtSquisherTester.nbtSmall);
    // Hardcoded hash value from previous runs
    private static final byte[] DATA_HASH = HashUtil.convertStringToHash(
        "f320c7fd475d4e59b116256575e17cd2ea5c792936536a5b12b21dbf05dcab77");

    @Test
    public void testStringToHash() {
        byte[] hash = HashUtil.convertStringToHash(STR);
        Assert.assertArrayEquals(HASH, hash);
    }

    @Test
    public void testHashToString() {
        String str = HashUtil.convertHashToString(HASH);
        Assert.assertEquals(STR, str);
    }

    @Test
    public void testHashBasic() {
        System.out.println(Arrays.toString(DATA));
        // We can't test this hash against anything -- the bpt format itself
        byte[] hash = HashUtil.computeHash(DATA);
        Assert.assertArrayEquals(DATA_HASH, hash);
    }
}
