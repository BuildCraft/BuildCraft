package buildcraft.test.core.lib.utils;

import static org.junit.Assert.*;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.FromDataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3i;

import buildcraft.core.Box;
import buildcraft.core.lib.utils.Matrix4i;

@RunWith(Theories.class)
public class Matrix4iTester {

    @DataPoints("rotate-y-indicies")
    public static final int[] testVectorIndicies = { 0, 1, 2 };

    private static final Vec3i[] testVectorInputs = { new Vec3i(0, 0, 0), new Vec3i(1, 1, 0), new Vec3i(7, 67, 20) };
    private static final Vec3i[] testVectorResults = { new Vec3i(0, 0, 0), new Vec3i(0, 1, -1), new Vec3i(20, 67, -7) };

    @Test
    public void testMakeTranslation() {
        Matrix4i mat = Matrix4i.makeTranslation(new Vec3i(1, 2, 3));

        Vec3i vec = mat.multiplyPosition(new Vec3i(10, 20, 30));

        assertEquals(11, vec.getX());
        assertEquals(22, vec.getY());
        assertEquals(33, vec.getZ());
    }

    @Test
    public void testMakeScale() {
        Matrix4i mat = Matrix4i.makeScale(new Vec3i(1, 2, 3));

        Vec3i vec = mat.multiplyPosition(new Vec3i(10, 20, 30));

        assertEquals(10, vec.getX());
        assertEquals(40, vec.getY());
        assertEquals(90, vec.getZ());
    }

    @Test
    @Theory
    public void testMakeRotY(@FromDataPoints("rotate-y-indicies") int index) {
        Matrix4i mat = Matrix4i.makeRotY(90);

        Vec3i in = testVectorInputs[index];
        Vec3i expected = testVectorResults[index];

        Vec3i result = mat.multiplyPosition(in);

        assertEquals(expected.getX(), result.getX());
        assertEquals(expected.getY(), result.getY());
        assertEquals(expected.getZ(), result.getZ());
    }

    @Test
    public void testLeftRotTranslate() {
        Box box = new Box(BlockPos.ORIGIN, new BlockPos(20, 3, 5));
        Box leftRotated = new Box(BlockPos.ORIGIN, new BlockPos(5, 3, 20));
        Matrix4i lRot = Matrix4i.makeRotLeftTranslatePositive(box);
        for (BlockPos pos : BlockPos.getAllInBox(box.min(), box.max())) {
            BlockPos rotated = lRot.multiplyPosition(pos);
            if (!leftRotated.contains(rotated)) Assert.fail(rotated + " was not inside the box!");
        }
    }

    @Test
    public void testMultiplyMatrix4i() {
        fail("Not yet implemented");
    }

    @Test
    public void testMultiplyPosition() {
        Vec3i original = new Vec3i(10, 20, 30);

        Vec3i multiplied = Matrix4i.IDENTITY.multiplyPosition(original);

        assertEquals(10, multiplied.getX());
        assertEquals(20, multiplied.getY());
        assertEquals(30, multiplied.getZ());
    }
}
