package buildcraft.lib.misc;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumFacing.AxisDirection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

/** Class for dealing with {@link Vec3d}, {@link Vec3i}, {@link EnumFacing}, {@link Axis} conversions and additions.
 * This is for simple functions ONLY, {@link PositionUtil} is for complex interactions */
public class VecUtil {
    public static final BlockPos POS_ONE = new BlockPos(1, 1, 1);
    public static final Vec3d VEC_HALF = new Vec3d(0.5, 0.5, 0.5);

    public static Vec3d add(Vec3d a, Vec3i b) {
        return a.addVector(b.getX(), b.getY(), b.getZ());
    }

    public static Vec3d offset(Vec3d from, EnumFacing direction, double by) {
        return from.addVector(direction.getFrontOffsetX() * by, direction.getFrontOffsetY() * by, direction.getFrontOffsetZ() * by);
    }

    public static double dot(Vec3d a, Vec3d b) {
        return a.xCoord * b.xCoord + a.yCoord * b.yCoord + a.zCoord * b.zCoord;
    }

    public static Vec3d scale(Vec3d vec, double scale) {
        return vec.scale(scale);
    }

    public static EnumFacing getFacing(Axis axis, boolean positive) {
        AxisDirection dir = positive ? AxisDirection.POSITIVE : AxisDirection.NEGATIVE;
        return EnumFacing.getFacingFromAxis(dir, axis);
    }

    public static BlockPos absolute(BlockPos val) {
        return new BlockPos(Math.abs(val.getX()), Math.abs(val.getY()), Math.abs(val.getZ()));
    }

    public static Vec3d replaceValue(Vec3d old, Axis axis, double with) {
        return new Vec3d(//
                axis == Axis.X ? with : old.xCoord,//
                axis == Axis.Y ? with : old.yCoord,//
                axis == Axis.Z ? with : old.zCoord//
        );
    }

    public static BlockPos replaceValue(Vec3i old, Axis axis, int with) {
        return new BlockPos(//
                axis == Axis.X ? with : old.getX(),//
                axis == Axis.Y ? with : old.getY(),//
                axis == Axis.Z ? with : old.getZ()//
        );
    }

    public static double getValue(Vec3d from, Axis axis) {
        return axis == Axis.X ? from.xCoord : axis == Axis.Y ? from.yCoord : from.zCoord;
    }

    public static int getValue(BlockPos from, Axis axis) {
        return axis == Axis.X ? from.getX() : axis == Axis.Y ? from.getY() : from.getZ();
    }

    public static Vec3d convertCenter(BlockPos pos) {
        return new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
    }

    public static BlockPos convertFloor(Vec3d vec) {
        return new BlockPos(Math.floor(vec.xCoord), Math.floor(vec.yCoord), Math.floor(vec.zCoord));
    }

    public static BlockPos convertCeiling(Vec3d vec) {
        return new BlockPos(Math.ceil(vec.xCoord), Math.ceil(vec.yCoord), Math.ceil(vec.zCoord));
    }

    // Min/Max

    public static BlockPos min(BlockPos a, BlockPos b) {
        if (a == null) return b;
        if (b == null) return a;
        return new BlockPos(//
                Math.min(a.getX(), b.getX()),//
                Math.min(a.getY(), b.getY()),//
                Math.min(a.getZ(), b.getZ())//
        );
    }

    public static BlockPos min(BlockPos a, BlockPos b, BlockPos c) {
        return min(min(a, b), c);
    }

    public static BlockPos min(BlockPos a, BlockPos b, BlockPos c, BlockPos d) {
        return min(min(a, b), min(c, d));
    }

    public static BlockPos max(BlockPos a, BlockPos b) {
        if (a == null) return b;
        if (b == null) return a;
        return new BlockPos(//
                Math.max(a.getX(), b.getX()),//
                Math.max(a.getY(), b.getY()),//
                Math.max(a.getZ(), b.getZ())//
        );
    }

    public static BlockPos max(BlockPos a, BlockPos b, BlockPos c) {
        return max(max(a, b), c);
    }

    public static BlockPos max(BlockPos a, BlockPos b, BlockPos c, BlockPos d) {
        return max(max(a, b), max(c, d));
    }
}
