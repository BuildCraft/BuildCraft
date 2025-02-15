package buildcraft.test.lib.misc;

import buildcraft.api.data.NbtSquishConstants;
import buildcraft.lib.misc.HashUtil;
import buildcraft.lib.nbt.NbtSquisher;
import buildcraft.test.lib.nbt.NbtSquisherTester;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;

public class HashUtilTester {
    private static final byte[] HASH = { 0, 1, 5, 9, (byte) 0xff, (byte) 0xbc };
    private static final String STR = "00010509ffbc";

    private static final byte[] DATA;
    // Hardcoded hash value from previous runs
    private static final byte[] DATA_HASH;

    static {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        NbtSquisher.squish(NbtSquisherTester.nbtSmall, NbtSquishConstants.VANILLA);
        DATA = baos.toByteArray();
        DATA_HASH = HashUtil.convertStringToHash("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855");
    }

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
        System.out.println(HashUtil.convertHashToString(hash));
        Assert.assertArrayEquals(DATA_HASH, hash);
    }
}
