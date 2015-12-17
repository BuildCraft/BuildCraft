package buildcraft.test.core.lib.utils;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import net.minecraft.util.BlockPos;

import buildcraft.core.Box;
import buildcraft.core.lib.utils.Utils;
import buildcraft.core.lib.utils.Utils.AxisOrder;
import buildcraft.core.lib.utils.Utils.EnumAxisOrder;

@RunWith(Theories.class)
public class UtilsTester {
    private static final BlockPos MIN = new BlockPos(1, 2, 3), MAX = new BlockPos(4, 5, 6);
    private static final int BLOCK_COUNT = 4 * 4 * 4;

    // @Test
    public void testIsFakePlayer() {
        fail("Not yet implemented");
    }

    // @Test
    public void testVec3() {
        fail("Not yet implemented");
    }

    // @Test
    public void testVec3i() {
        fail("Not yet implemented");
    }

    // @Test
    public void testVec3f() {
        fail("Not yet implemented");
    }

    // @Test
    public void testConvertVec3i() {
        fail("Not yet implemented");
    }

    // @Test
    public void testConvertMiddle() {
        fail("Not yet implemented");
    }

    // @Test
    public void testConvertEnumFacing() {
        fail("Not yet implemented");
    }

    // @Test
    public void testConvertEnumFacingDouble() {
        fail("Not yet implemented");
    }

    // @Test
    public void testConvertExcept() {
        fail("Not yet implemented");
    }

    // @Test
    public void testConvertPositive() {
        fail("Not yet implemented");
    }

    // @Test
    public void testOther() {
        fail("Not yet implemented");
    }

    // @Test
    public void testConvertFloorVec3() {
        fail("Not yet implemented");
    }

    // @Test
    public void testConvertCeiling() {
        fail("Not yet implemented");
    }

    // @Test
    public void testConvertFloorEnumFacing() {
        fail("Not yet implemented");
    }

    // @Test
    public void testConvertFloorEnumFacingInt() {
        fail("Not yet implemented");
    }

    // @Test
    public void testMinBlockPosBlockPos() {
        fail("Not yet implemented");
    }

    // @Test
    public void testMaxBlockPosBlockPos() {
        fail("Not yet implemented");
    }

    // @Test
    public void testConvertVector3f() {
        fail("Not yet implemented");
    }

    // @Test
    public void testConvertFloat() {
        fail("Not yet implemented");
    }

    // @Test
    public void testMultiply() {
        fail("Not yet implemented");
    }

    // @Test
    public void testDivide() {
        fail("Not yet implemented");
    }

    // @Test
    public void testClamp() {
        fail("Not yet implemented");
    }

    // @Test
    public void testMinVec3Vec3() {
        fail("Not yet implemented");
    }

    // @Test
    public void testMaxVec3Vec3() {
        fail("Not yet implemented");
    }

    // @Test
    public void testToMatrix() {
        fail("Not yet implemented");
    }

    // @Test
    public void testWithValueVec3AxisDouble() {
        fail("Not yet implemented");
    }

    // @Test
    public void testGetValueVec3Axis() {
        fail("Not yet implemented");
    }

    // @Test
    public void testWithValueBlockPosAxisInt() {
        fail("Not yet implemented");
    }

    // @Test
    public void testGetValueBlockPosAxis() {
        fail("Not yet implemented");
    }

    // @Test
    public void testMinAxisAlignedBB() {
        fail("Not yet implemented");
    }

    // @Test
    public void testMaxAxisAlignedBB() {
        fail("Not yet implemented");
    }

    // @Test
    public void testAllChunksFor() {
        fail("Not yet implemented");
    }

    // @Test
    public void testAllInChunk() {
        fail("Not yet implemented");
    }

    // @Test
    public void testGetMinForFaceEnumFacingVec3Vec3() {
        fail("Not yet implemented");
    }

    // @Test
    public void testGetMaxForFaceEnumFacingVec3Vec3() {
        fail("Not yet implemented");
    }

    // @Test
    public void testGetMinForFaceEnumFacingBlockPosBlockPos() {
        fail("Not yet implemented");
    }

    // @Test
    public void testGetMaxForFaceEnumFacingBlockPosBlockPos() {
        fail("Not yet implemented");
    }

    // @Test
    public void testIsInside() {
        fail("Not yet implemented");
    }

    // @Test
    public void testGetClosestInsideBlockPosBlockPosBlockPos() {
        fail("Not yet implemented");
    }

    // @Test
    public void testGetClosestInsideBoxBlockPos() {
        fail("Not yet implemented");
    }

    // @Test
    public void testBoundingBox() {
        fail("Not yet implemented");
    }

    // @Test
    public void testGetNeighboursIncludingSelf() {
        fail("Not yet implemented");
    }

    // @Test
    public void testGetNeighbours() {
        fail("Not yet implemented");
    }

    // @Test
    public void testRandomBlockPos() {
        fail("Not yet implemented");
    }

    @Test
    public void testInvert() {
        BlockPos a = new BlockPos(0, 18, -1267812);
        BlockPos b = Utils.invert(a);
        assertEquals(0, b.getX());
        assertEquals(-18, b.getY());
        assertEquals(1267812, b.getZ());
    }

    // @Test
    public void testGetFacing() {
        fail("Not yet implemented");
    }

    @DataPoints
    public static List<AxisOrder> getOrders() {
        List<AxisOrder> orders = Lists.newArrayList();
        boolean[] pos = { false, true };
        for (EnumAxisOrder e : EnumAxisOrder.values()) {
            for (boolean a : pos)
                for (boolean b : pos)
                    for (boolean c : pos) {
                        orders.add(new AxisOrder(e, a, b, c));
                    }
        }
        return orders;
    }

    @Test
    @Theory
    public void testGetAllInBox(AxisOrder order) {
        System.out.println("Starting test: getAllInBox. Order =  " + order);

        Box box = new Box(MIN, MAX);
        Set<BlockPos> positions = Sets.newHashSet();
        for (BlockPos pos : Utils.getAllInBox(MIN, MAX, order)) {
            if (positions.contains(pos)) fail("The set already contained " + pos);
            if (!box.contains(pos)) fail("Tried to iterate with a positon that was outside of the box! " + pos);
            positions.add(pos);
        }
        if (positions.size() != BLOCK_COUNT) fail("Needed " + BLOCK_COUNT + " unique positions, but only found " + positions.size());
    }
}
