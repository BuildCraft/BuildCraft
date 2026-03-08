package buildcraft.test.lib.misc.data;

import java.util.Random;

import net.minecraft.util.math.BlockPos;
import org.junit.Assert;
import org.junit.Test;

import buildcraft.lib.misc.data.AxisOrder;
import buildcraft.lib.misc.data.AxisOrder.Inversion;
import buildcraft.lib.misc.data.BoxIterator;
import buildcraft.lib.misc.data.EnumAxisOrder;

public class BoxIteratorTester {

    @Test
    public void testNormalMoving() {
        for (int sx = 1; sx < 6; sx++) {
            for (int sy = 1; sy < 6; sy++) {
                for (int sz = 1; sz < 6; sz++) {
                    testNormalMoving(sx, sy, sz);
                }
            }
        }
    }

    private static void testNormalMoving(int sx, int sy, int sz) {
        BoxIterator bi = new BoxIterator(
//            BlockPos.ORIGIN, new BlockPos(sx - 1, sy - 1, sz - 1), AxisOrder.getFor(EnumAxisOrder.XYZ, Inversion.PPP), false
            BlockPos.ZERO, new BlockPos(sx - 1, sy - 1, sz - 1), AxisOrder.getFor(EnumAxisOrder.XYZ, Inversion.PPP), false
        );

        Random rand = new Random(42);
        for (int i = 0; i < 200; i++) {
            BlockPos bpos = new BlockPos(rand.nextInt(sx), rand.nextInt(sy), rand.nextInt(sz));
            bi.moveTo(bpos);
            Assert.assertEquals(bpos, bi.advance());
        }
    }
}
