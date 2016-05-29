package buildcraft.lib.misc;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

/** Class for dealing with {@link Vec3d}, {@link Vec3i}, {@link EnumFacing}, {@link Axis} conversions and additions.
 * This is for simple functions ONLY, {@link PositionUtil} is for complex interactions */
public class VecUtil {
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
        return new Vec3d(vec.xCoord * scale, vec.yCoord * scale, vec.zCoord * scale);
    }

    public static EnumFacing getFacing(Axis axis, boolean positive) {
        if (axis == Axis.X) return positive ? EnumFacing.EAST : EnumFacing.WEST;
        if (axis == Axis.Y) return positive ? EnumFacing.UP : EnumFacing.DOWN;
        if (axis == Axis.Z) return positive ? EnumFacing.SOUTH : EnumFacing.NORTH;
        return null;
    }
}
