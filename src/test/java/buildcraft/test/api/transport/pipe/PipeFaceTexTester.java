package buildcraft.test.api.transport.pipe;

import buildcraft.api.transport.pipe.PipeFaceTex;
import org.junit.Assert;
import org.junit.Test;

public class PipeFaceTexTester {
    @Test
    public void testBasicHashCodes() {
        for (int i = 0; i < 1000; i++) {
            assertEquals(PipeFaceTex.___testing_create_single(i), PipeFaceTex.get(i));
        }

        assertEquals(PipeFaceTex.get(0), PipeFaceTex.get(new int[] { 0 }, -1));
    }

    private static void assertEquals(PipeFaceTex a, PipeFaceTex b) {
        Assert.assertEquals(a.hashCode(), b.hashCode());
        Assert.assertEquals(a, b);
    }
}
