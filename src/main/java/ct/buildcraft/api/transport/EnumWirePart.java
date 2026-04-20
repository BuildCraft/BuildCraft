package ct.buildcraft.api.transport;

import net.minecraft.core.Direction;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public enum EnumWirePart {
    EAST_UP_SOUTH(true, true, true),
    EAST_UP_NORTH(true, true, false),
    EAST_DOWN_SOUTH(true, false, true),
    EAST_DOWN_NORTH(true, false, false),
    WEST_UP_SOUTH(false, true, true),
    WEST_UP_NORTH(false, true, false),
    WEST_DOWN_SOUTH(false, false, true),
    WEST_DOWN_NORTH(false, false, false);

    public static final EnumWirePart[] VALUES = values();

    public final AxisDirection x, y, z;

    /** The bounding box for rendering a wire or selecting an already-placed wire. */
    public final VoxelShape boundingBox;

    /** The bounding box that is used when adding pipe wire to a pipe */
    public final VoxelShape boundingBoxPossible;

    EnumWirePart(boolean x, boolean y, boolean z) {
        this.x = x ? AxisDirection.POSITIVE : AxisDirection.NEGATIVE;
        this.y = y ? AxisDirection.POSITIVE : AxisDirection.NEGATIVE;
        this.z = z ? AxisDirection.POSITIVE : AxisDirection.NEGATIVE;
        double x1 = this.x.getStep() * (5 / 16.0) + 0.5;
        double y1 = this.y.getStep() * (5 / 16.0) + 0.5;
        double z1 = this.z.getStep() * (5 / 16.0) + 0.5;
        double x2 = this.x.getStep() * (4 / 16.0) + 0.5;
        double y2 = this.y.getStep() * (4 / 16.0) + 0.5;
        double z2 = this.z.getStep() * (4 / 16.0) + 0.5;
        this.boundingBox = Shapes.create(new AABB(x1, y1, z1, x2, y2, z2));

        Vec3 center = new Vec3(0.5, 0.5, 0.5);
        Vec3 edge = new Vec3(x ? 0.75 : 0.25, y ? 0.75 : 0.25, z ? 0.75 : 0.25);
        this.boundingBoxPossible = Shapes.create(new AABB(center.x, center.y, center.z, edge.x,
                edge.y, edge.z));
    }

    public AxisDirection getDirection(Direction.Axis axis) {
        switch (axis) {
            case X:
                return x;
            case Y:
                return y;
            case Z:
                return z;
            default:
                return null;
        }
    }

    public static EnumWirePart get(int x, int y, int z) {
        boolean bx = (x % 2 + 2) % 2 == 1;
        boolean by = (y % 2 + 2) % 2 == 1;
        boolean bz = (z % 2 + 2) % 2 == 1;
        return get(bx, by, bz);
    }

    public static EnumWirePart get(boolean x, boolean y, boolean z) {
        if (x) {
            if (y) {
                return z ? EAST_UP_SOUTH : EAST_UP_NORTH;
            } else {
                return z ? EAST_DOWN_SOUTH : EAST_DOWN_NORTH;
            }
        } else {
            if (y) {
                return z ? WEST_UP_SOUTH : WEST_UP_NORTH;
            } else {
                return z ? WEST_DOWN_SOUTH : WEST_DOWN_NORTH;
            }
        }
    }
    
    public EnumWirePart rotate(Rotation rotation) {
        if (rotation == Rotation.NONE) {
            return this;
        }
        
        // 获取当前三个轴的方向（作为布尔值，true=正方向，false=负方向）
        boolean currentX = this.x == AxisDirection.POSITIVE;
        boolean currentY = this.y == AxisDirection.POSITIVE;
        boolean currentZ = this.z == AxisDirection.POSITIVE;
        
        // Y 轴方向不变（绕 Y 轴旋转）
        boolean newY = currentY;
        
        boolean newX, newZ;
        
        switch (rotation) {
            case CLOCKWISE_90:
                // 顺时针旋转90°: X -> Z, Z -> -X
                // 原 +X 方向变为 +Z，原 -X 方向变为 -Z
                // 原 +Z 方向变为 -X，原 -Z 方向变为 +X
                newX = !currentZ;  // Z 轴取反后给 X
                newZ = currentX;   // X 轴直接给 Z
                break;
                
            case CLOCKWISE_180:
                // 旋转180°: X -> -X, Z -> -Z
                newX = !currentX;
                newZ = !currentZ;
                break;
                
            case COUNTERCLOCKWISE_90:
                // 逆时针旋转90°: X -> -Z, Z -> X
                newX = currentZ;   // Z 轴直接给 X
                newZ = !currentX;  // X 轴取反后给 Z
                break;
                
            default:
                return this;
        }
        
        return get(newX, newY, newZ);
    }
}
