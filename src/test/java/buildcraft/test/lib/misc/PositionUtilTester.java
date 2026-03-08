package buildcraft.test.lib.misc;

import buildcraft.lib.misc.PositionUtil;
import buildcraft.lib.misc.VecUtil;
import com.google.common.collect.ImmutableList;
import net.minecraft.core.BlockPos;
import org.junit.Assert;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.FromDataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import java.util.HashSet;

@RunWith(Theories.class)
public class PositionUtilTester {

    @DataPoints({ "paths" })
    public static final BlockPos[][] PATH_DATA_POINTS = { //
            { new BlockPos(0, 0, 0), new BlockPos(0, 0, 0) }, //
            { new BlockPos(0, 1, 0), new BlockPos(0, 0, 0) }, //
            { new BlockPos(0, 0, 0), new BlockPos(1, 4, 6) }, //
            { new BlockPos(0, 0, 0), new BlockPos(1, 0, 0) }, //
            { new BlockPos(0, 0, 0), new BlockPos(1, 1, 1) }, //
            { new BlockPos(-50, 45, 34), new BlockPos(-37, 7, -40) }, //
    };

    @DataPoints({ "boxes" })
    public static final BlockPos[][] BOX_DATA_POINTS = { //
            { new BlockPos(0, 0, 0), new BlockPos(0, 0, 0) }, //
            { new BlockPos(0, 0, 0), new BlockPos(1, 0, 0) }, //
            { new BlockPos(0, 0, 0), new BlockPos(0, 1, 0) }, //
            { new BlockPos(0, 0, 0), new BlockPos(0, 0, 1) }, //
            { new BlockPos(0, 0, 0), new BlockPos(2, 0, 0) }, //
            { new BlockPos(0, 0, 0), new BlockPos(0, 2, 0) }, //
            { new BlockPos(0, 0, 0), new BlockPos(0, 0, 2) }, //
            { new BlockPos(0, 0, 0), new BlockPos(3, 0, 0) }, //
            { new BlockPos(0, 0, 0), new BlockPos(0, 3, 0) }, //
            { new BlockPos(0, 0, 0), new BlockPos(0, 0, 3) }, //
            { new BlockPos(0, 0, 0), new BlockPos(3, 3, 0) }, //
            { new BlockPos(0, 0, 0), new BlockPos(3, 0, 3) }, //
            { new BlockPos(0, 0, 0), new BlockPos(0, 3, 3) }, //
            { new BlockPos(-45, 3, -4), new BlockPos(-38, 16, 16) }, //
    };

    @Theory
    public void testNormalDataPoint(@FromDataPoints("paths") BlockPos[] path) {
        BlockPos from = path[0];
        BlockPos to = path[1];
        System.out.println("All from " + from + " to " + to);
        for (BlockPos p : PositionUtil.getAllOnPath(from, to)) {
            System.out.println("  - " + p);
        }
    }

    @Theory
    public void testBounds(@FromDataPoints("boxes") BlockPos[] box) {
        BlockPos min = box[0];
        BlockPos max = box[1];
        System.out.println("Box = [ " + min + " -> " + max + " ]");
        ImmutableList<BlockPos> allOnEdge = PositionUtil.getAllOnEdge(min, max);
        String info = "\nmin = " + min + ",\nmax = " + max + ",\nonEdge = \n"
                + allOnEdge.toString().replace("}, Block", "},\n Block");

        // Ensure that the returned list has no duplicates
        Assert.assertEquals("Duplicates! " + info, new HashSet<>(allOnEdge).size(), allOnEdge.size());
        Assert.assertEquals("Count! " + info, allOnEdge.size(), PositionUtil.getCountOnEdge(min, max));

        // Ensure that all of them are valid edges and faces
        for (BlockPos p : allOnEdge) {
            String info2 = "pos = " + p + ", " + info;
            Assert.assertEquals("isOnEdge mismatch! " + info2, true, PositionUtil.isOnEdge(min, max, p));
            Assert.assertEquals("isOnFace mismatch! " + info2, true, PositionUtil.isOnFace(min, max, p));
        }

        // Construct it manually via PositionUtil.isOnEdge

//        for (BlockPos p : BlockPos.getAllInBox(min.subtract(VecUtil.POS_ONE), max.add(VecUtil.POS_ONE)))
        for (BlockPos p : BlockPos.betweenClosed(min.subtract(VecUtil.POS_ONE), max.offset(VecUtil.POS_ONE))) {
            if (PositionUtil.isOnEdge(min, max, p)) {
                Assert.assertTrue("All In Box", allOnEdge.contains(p));
            }
        }
    }
}
