package buildcraft.lib.misc;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

import buildcraft.lib.misc.data.Box;

public class Matrix4i {
    public static final Matrix4i IDENTITY = makeScale(VecUtil.POS_ONE);

    private final int m00, m01, m02, m03;
    private final int m10, m11, m12, m13;
    private final int m20, m21, m22, m23;
    private final int m30, m31, m32, m33;

    public static Matrix4i makeTranslation(Vec3i vec) {
        // @formatter:off
        return new Matrix4i(
                1, 0, 0, vec.getX(),
                0, 1, 0, vec.getY(),
                0, 0, 1, vec.getZ(),
                0, 0, 0, 1);
        // @formatter:on
    }

    public static Matrix4i makeScale(Vec3i vec) {
        // @formatter:off
        return new Matrix4i(
                vec.getX(), 0,          0,          0,
                0,          vec.getY(), 0,          0,
                0,          0,          vec.getZ(), 0,
                0,          0,          0,          1);
        // @formatter:on
    }

    public static Matrix4i makeRotY(int ang) {
        if (ang % 90 != 0) throw new IllegalArgumentException("You can only have angles in increments of 90 degrees!");
        ang %= 360;

        if (ang == 90) {
            // @formatter:off
            return new Matrix4i(
                    0, 0, 1, 0,
                    0, 1, 0, 0,
                   -1, 0, 0, 0,
                    0, 0, 0, 1);
            // @formatter:on
        } else if (ang == 180) {
            // @formatter:off
            return new Matrix4i(
                   -1, 0, 0, 0,
                    0, 1, 0, 0,
                    0, 0,-1, 0,
                    0, 0, 0, 1);
            // @formatter:on
        } else if (ang == 270) {
            // @formatter:off
            return new Matrix4i(
                    0, 0,-1, 0,
                    0, 1, 0, 0,
                    1, 0, 0, 0,
                    0, 0, 0, 1);
            // @formatter:on
        } else return IDENTITY;
    }

    /** @return A matrix that will apply a left rotation to points and then translate backwards, effectively applying a
     *         left rotation in-place to the given box around its center point. */
    public static Matrix4i makeRotLeftTranslatePositive(Box box) {
        BlockPos translation = box.min();
        Matrix4i translateBack = makeTranslation(BlockPos.ORIGIN.subtract(translation));
        Matrix4i rotY = makeRotY(270);
        Matrix4i translateForth = makeTranslation(translation.add(new Vec3i(box.size().getZ() - 1, 0, 0)));

        Matrix4i total = translateForth.multiply(rotY).multiply(translateBack);
        return total;
    }

    public static Matrix4i multiply(Matrix4i m1, Matrix4i m2) {
        int m00 = m1.m00 * m2.m00 + m1.m01 * m2.m10 + m1.m02 * m2.m20 + m1.m03 * m2.m30;
        int m01 = m1.m00 * m2.m01 + m1.m01 * m2.m11 + m1.m02 * m2.m21 + m1.m03 * m2.m31;
        int m02 = m1.m00 * m2.m02 + m1.m01 * m2.m12 + m1.m02 * m2.m22 + m1.m03 * m2.m32;
        int m03 = m1.m00 * m2.m03 + m1.m01 * m2.m13 + m1.m02 * m2.m23 + m1.m03 * m2.m33;

        int m10 = m1.m10 * m2.m00 + m1.m11 * m2.m10 + m1.m12 * m2.m20 + m1.m13 * m2.m30;
        int m11 = m1.m10 * m2.m01 + m1.m11 * m2.m11 + m1.m12 * m2.m21 + m1.m13 * m2.m31;
        int m12 = m1.m10 * m2.m02 + m1.m11 * m2.m12 + m1.m12 * m2.m22 + m1.m13 * m2.m32;
        int m13 = m1.m10 * m2.m03 + m1.m11 * m2.m13 + m1.m12 * m2.m23 + m1.m13 * m2.m33;

        int m20 = m1.m20 * m2.m00 + m1.m21 * m2.m10 + m1.m22 * m2.m20 + m1.m23 * m2.m30;
        int m21 = m1.m20 * m2.m01 + m1.m21 * m2.m11 + m1.m22 * m2.m21 + m1.m23 * m2.m31;
        int m22 = m1.m20 * m2.m02 + m1.m21 * m2.m12 + m1.m22 * m2.m22 + m1.m23 * m2.m32;
        int m23 = m1.m20 * m2.m03 + m1.m21 * m2.m13 + m1.m22 * m2.m23 + m1.m23 * m2.m33;

        int m30 = m1.m30 * m2.m00 + m1.m31 * m2.m10 + m1.m32 * m2.m20 + m1.m33 * m2.m30;
        int m31 = m1.m30 * m2.m01 + m1.m31 * m2.m11 + m1.m32 * m2.m21 + m1.m33 * m2.m31;
        int m32 = m1.m30 * m2.m02 + m1.m31 * m2.m12 + m1.m32 * m2.m22 + m1.m33 * m2.m32;
        int m33 = m1.m30 * m2.m03 + m1.m31 * m2.m13 + m1.m32 * m2.m23 + m1.m33 * m2.m33;

        // @formatter:off
        return new Matrix4i(
                m00, m01, m02, m03,
                m10, m11, m12, m13,
                m20, m21, m22, m23,
                m30, m31, m32, m33);
        // @formatter:on
    }

    // @formatter:off
    public Matrix4i(
            int i00, int i01, int i02, int i03, 
            int i10, int i11, int i12, int i13,
            int i20, int i21, int i22, int i23,
            int i30, int i31, int i32, int i33) {
        m00 = i00; m01 = i01; m02 = i02; m03 = i03;
        m10 = i10; m11 = i11; m12 = i12; m13 = i13;
        m20 = i20; m21 = i21; m22 = i22; m23 = i23;
        m30 = i30; m31 = i31; m32 = i32; m33 = i33;
    }
    // @formatter:on

    public Matrix4i multiply(Matrix4i m1) {
        return multiply(this, m1);
    }

    /** Multiplies the position by the matrix, returning a new position. This assumes the 4th part of the position is
     * 1 */
    public BlockPos multiplyPosition(Vec3i pos) {
        int x = pos.getX() * m00 + pos.getY() * m01 + pos.getZ() * m02 + m03;
        int y = pos.getX() * m10 + pos.getY() * m11 + pos.getZ() * m12 + m13;
        int z = pos.getX() * m20 + pos.getY() * m21 + pos.getZ() * m22 + m23;
        BlockPos end = new BlockPos(x, y, z);
        return end;
    }

    public Matrix4i addTranslation(Vec3i trans) {
        return multiply(makeTranslation(trans));
    }

    public Matrix4i addScale(Vec3i scale) {
        return multiply(makeScale(scale));
    }

    @Override
    public String toString() {
        return m00 + ", " + m01 + ", " + m02 + ", " + m03 + "\n" + m10 + ", " + m11 + ", " + m12 + ", " + m13 + "\n" + m20 + ", " + m21 + ", " + m22 + ", " + m23 + "\n" + m30 + ", " + m31 + ", " + m32 + ", " + m33;
    }
}
