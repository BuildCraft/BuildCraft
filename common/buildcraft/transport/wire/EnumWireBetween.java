package buildcraft.transport.wire;

import java.util.Arrays;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumFacing.AxisDirection;
import net.minecraft.util.math.AxisAlignedBB;

/** Holds all of the possible boxes that a wire can occupy - excluding the ones in EnumWirePart. */
public enum EnumWireBetween {
    // Centre
    X_UP_SOUTH(Axis.X, true, true),
    X_UP_NORTH(Axis.X, true, false),
    X_DOWN_SOUTH(Axis.X, false, true),
    X_DOWN_NORTH(Axis.X, false, false),

    Y_SOUTH_EAST(Axis.Y, true, true),
    Y_SOUTH_WEST(Axis.Y, true, false),
    Y_NORTH_EAST(Axis.Y, false, true),
    Y_NORTH_WEST(Axis.Y, false, false),

    Z_UP_EAST(Axis.Z, true, true),
    Z_UP_WEST(Axis.Z, true, false),
    Z_DOWN_EAST(Axis.Z, false, true),
    Z_DOWN_WEST(Axis.Z, false, false),

    // Between pipes
    EAST_UP_SOUTH(EnumFacing.EAST, true, true),
    EAST_UP_NORTH(EnumFacing.EAST, true, false),
    EAST_DOWN_SOUTH(EnumFacing.EAST, false, true),
    EAST_DOWN_NORTH(EnumFacing.EAST, false, false),

    WEST_UP_SOUTH(EnumFacing.WEST, true, true),
    WEST_UP_NORTH(EnumFacing.WEST, true, false),
    WEST_DOWN_SOUTH(EnumFacing.WEST, false, true),
    WEST_DOWN_NORTH(EnumFacing.WEST, false, false),

    UP_SOUTH_EAST(EnumFacing.UP, true, true),
    UP_SOUTH_WEST(EnumFacing.UP, true, false),
    UP_NORTH_EAST(EnumFacing.UP, false, true),
    UP_NORTH_WEST(EnumFacing.UP, false, false),

    DOWN_SOUTH_EAST(EnumFacing.DOWN, true, true),
    DOWN_SOUTH_WEST(EnumFacing.DOWN, true, false),
    DOWN_NORTH_EAST(EnumFacing.DOWN, false, true),
    DOWN_NORTH_WEST(EnumFacing.DOWN, false, false),

    SOUTH_UP_EAST(EnumFacing.SOUTH, true, true),
    SOUTH_UP_WEST(EnumFacing.SOUTH, true, false),
    SOUTH_DOWN_EAST(EnumFacing.SOUTH, false, true),
    SOUTH_DOWN_WEST(EnumFacing.SOUTH, false, false),

    NORTH_UP_EAST(EnumFacing.NORTH, true, true),
    NORTH_UP_WEST(EnumFacing.NORTH, true, false),
    NORTH_DOWN_EAST(EnumFacing.NORTH, false, true),
    NORTH_DOWN_WEST(EnumFacing.NORTH, false, false);

    public static final EnumWireBetween[] VALUES = values();
    public static final EnumWireBetween[] CENTRES = Arrays.copyOfRange(VALUES, 0, 12, EnumWireBetween[].class);
    public static final EnumWireBetween[] CONNECTIONS = Arrays.copyOfRange(VALUES, 12, 36, EnumWireBetween[].class);

    public final Axis mainAxis;
    public final AxisAlignedBB boundingBox;

    EnumWireBetween(Axis mainAxis, boolean xy, boolean yz) {
        this.mainAxis = mainAxis;
        int x1 = mainAxis == Axis.X ? 4 : (xy ? 12 : 3);
        int y1 = mainAxis == Axis.Y ? 4 : ((mainAxis == Axis.X ? xy : yz) ? 12 : 3);
        int z1 = mainAxis == Axis.Z ? 4 : (yz ? 12 : 3);
        int x2 = x1 + (mainAxis == Axis.X ? 8 : 1);
        int y2 = y1 + (mainAxis == Axis.Y ? 8 : 1);
        int z2 = z1 + (mainAxis == Axis.Z ? 8 : 1);
        boundingBox = new AxisAlignedBB(x1 / 16.0, y1 / 16.0, z1 / 16.0, x2 / 16.0, y2 / 16.0, z2 / 16.0);
    }

    EnumWireBetween(EnumFacing to, boolean xy, boolean yz) {
        this.mainAxis = to.getAxis();
        int start = to.getAxisDirection() == AxisDirection.POSITIVE ? 12 : 0;
        int x1 = mainAxis == Axis.X ? start : (xy ? 12 : 3);
        int y1 = mainAxis == Axis.Y ? start : ((mainAxis == Axis.X ? xy : yz) ? 12 : 3);
        int z1 = mainAxis == Axis.Z ? start : (yz ? 12 : 3);
        int x2 = x1 + (mainAxis == Axis.X ? 4 : 1);
        int y2 = y1 + (mainAxis == Axis.Y ? 4 : 1);
        int z2 = z1 + (mainAxis == Axis.Z ? 4 : 1);
        boundingBox = new AxisAlignedBB(x1 / 16.0, y1 / 16.0, z1 / 16.0, x2 / 16.0, y2 / 16.0, z2 / 16.0);
    }
}
