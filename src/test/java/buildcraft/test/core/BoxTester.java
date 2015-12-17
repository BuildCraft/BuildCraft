package buildcraft.test.core;

import static org.junit.Assert.*;

import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.ImmutableMap;

import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.FromDataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;

import buildcraft.core.Box;
import buildcraft.test.TestHelper;

@RunWith(Theories.class)
public class BoxTester {
    private static final BlockPos MIN = new BlockPos(1, 2, 3), MAX = new BlockPos(4, 5, 6);
    private static final BlockPos SIZE = new BlockPos(4, 4, 4);
    private static final BlockPos CENTER = new BlockPos(3, 4, 5);
    private static final Vec3 CENTER_EXACT = new Vec3(3, 4, 5);
    private static final BlockPos ROTATED_MIN = new BlockPos(1, 2, 3);
    private static final BlockPos ROTATED_MAX = new BlockPos(4, 5, 6);

//    @Test
    public void testExtendToEncompassBoth() {
        fail("Not yet implemented");
    }

//    @Test
    public void testSetMin() {
        fail("Not yet implemented");
    }

//    @Test
    public void testSetMax() {
        fail("Not yet implemented");
    }

//    @Test
    public void testInitializeBox() {
        fail("Not yet implemented");
    }

//    @Test
    public void testInitializeNBTTagCompound() {
        fail("Not yet implemented");
    }

//    @Test
    public void testInitializeCenterBlockPosInt() {
        fail("Not yet implemented");
    }

//    @Test
    public void testInitializeCenterBlockPosVec3i() {
        fail("Not yet implemented");
    }

//    @Test
    public void testGetBlocksInArea() {
        fail("Not yet implemented");
    }

//    @Test
    public void testExpand() {
        fail("Not yet implemented");
    }

//    @Test
    public void testContract() {
        fail("Not yet implemented");
    }

    @DataPoints("testContainsVec3")
    public static Set<Entry<Vec3, Boolean>> dataContainsVec3() {
        return ImmutableMap.<Vec3, Boolean> builder()
                // @formatter:off
                .put(new Vec3(0, 0, 0), false)
                .put(new Vec3(1, 2, 3), true)
                .put(new Vec3(1.3, 2.4, 3.5), true)
                .put(new Vec3(4.9, 5.9, 6.9), true)
                .put(new Vec3(5, 5, 6), false)
                // @formatter:on
                .build().entrySet();
    }

    @Test
    @Theory
    public void testContainsVec3(@FromDataPoints("testContainsVec3") Entry<Vec3, Boolean> entry) {
        Box box = new Box(MIN, MAX);
        Vec3 in = entry.getKey();
        boolean expected = entry.getValue();
        assertEquals(expected, box.contains(in));
    }

    public static final BlockPos[] containsBlockPosTests = {};

//    @Test
//    @Theory
    public void testContainsBlockPos() {
        fail("Not yet implemented");
    }

    @Test
    public void testMin() {
        Box box = new Box(MIN, MAX);
        assertEquals(MIN, box.min());
    }

    @Test
    public void testMax() {
        Box box = new Box(MIN, MAX);
        assertEquals(MAX, box.max());
    }

    @Test
    public void testSize() {
        Box box = new Box(MIN, MAX);
        assertEquals(SIZE, box.size());
    }

    @Test
    public void testCenter() {
        Box box = new Box(MIN, MAX);
        assertEquals(CENTER, box.center());
    }

    @Test
    public void testCenterExact() {
        Box box = new Box(MIN, MAX);
        TestHelper.assertVec3Equals(CENTER_EXACT, box.centerExact());
    }

    @Test
    public void testRotateLeft() {
        Box box = new Box(MIN, MAX);
        Box rotated = box.rotateLeft();
        BlockPos rotMin = rotated.min();
        BlockPos rotMax = rotated.max();
        assertEquals(ROTATED_MIN, rotMin);
        assertEquals(ROTATED_MAX, rotMax);
    }

//    @Test
    public void testExtendToEncompassIBox() {
        fail("Not yet implemented");
    }

//    @Test
    public void testGetBoundingBox() {
        fail("Not yet implemented");
    }

//    @Test
    public void testExtendToEncompassVec3() {
        fail("Not yet implemented");
    }

//    @Test
    public void testExtendToEncompassBlockPos() {
        fail("Not yet implemented");
    }

//    @Test
    public void testDistanceTo() {
        fail("Not yet implemented");
    }

//    @Test
    public void testDistanceToSquared() {
        fail("Not yet implemented");
    }

//    @Test
    public void testClosestInsideTo() {
        fail("Not yet implemented");
    }

//    @Test
    public void testGetRandomBlockPos() {
        fail("Not yet implemented");
    }

}
