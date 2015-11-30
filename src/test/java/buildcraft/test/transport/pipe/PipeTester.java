package buildcraft.test.transport.pipe;

import static org.junit.Assert.fail;

import org.junit.Test;

import buildcraft.transport.BlockGenericPipe;

public class PipeTester {
    @Test
    public void test() {
        if (BlockGenericPipe.isValid(null)) {
            fail("haha lol");
        }
    }
}
