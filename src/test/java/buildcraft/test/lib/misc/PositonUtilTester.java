package buildcraft.test.lib.misc;

import org.junit.Test;

import net.minecraft.util.math.BlockPos;

import buildcraft.lib.misc.PositionUtil;

public class PositonUtilTester {
    @Test
    public void testPath1() {
        testPath(new BlockPos(0, 1, 0), new BlockPos(1, 4, 6));
    }

    @Test
    public void testPath2() {
        testPath(new BlockPos(0, 0, 0), new BlockPos(0, 0, 0));
    }

    @Test
    public void testPath3() {
        testPath(new BlockPos(0, 0, 0), new BlockPos(1, 0, 0));
    }

    @Test
    public void testPath4() {
        testPath(new BlockPos(0, 0, 0), new BlockPos(1, 1, 1));
    }

    @Test
    public void testPath5() {
        testPath(new BlockPos(-10, 4, -30), new BlockPos(-13, 4, -40));
    }

    @Test
    public void testPath6() {
        testPath(new BlockPos(0, 3, 0), new BlockPos(-1, 3, 10));
    }

    private static void testPath(BlockPos from, BlockPos to) {
        System.out.println("All from " + from + " to " + to);
        for (BlockPos p : PositionUtil.getAllOnPath(from, to)) {
            System.out.println(p);
        }
    }
}
