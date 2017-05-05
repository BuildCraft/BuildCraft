package buildcraft.lib.misc;

import buildcraft.lib.net.PacketBufferBC;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializer;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;

import java.io.IOException;

public class RotationUtil {
    public static final DataSerializer<EnumFacing.Axis> AXIS_SERIALIZER = new DataSerializer<EnumFacing.Axis>() {
        @Override
        public void write(PacketBuffer buf, EnumFacing.Axis value) {
            new PacketBufferBC(buf).writeEnumValue(value);
        }

        @Override
        public EnumFacing.Axis read(PacketBuffer buf) throws IOException {
            return new PacketBufferBC(buf).readEnumValue(EnumFacing.Axis.class);
        }

        @Override
        public DataParameter<EnumFacing.Axis> createKey(int id) {
            return new DataParameter<>(id, this);
        }
    };

    static {
        DataSerializers.registerSerializer(AXIS_SERIALIZER);
    }

    public static AxisAlignedBB rotateAABB(AxisAlignedBB aabb, EnumFacing facing) {
        if (facing == EnumFacing.DOWN) {
            return new AxisAlignedBB(aabb.minX, aabb.maxY, aabb.minZ, aabb.maxX, aabb.minY, aabb.maxZ);
        } else if (facing == EnumFacing.UP) {
            return new AxisAlignedBB(aabb.minX, 1 - aabb.maxY, aabb.minZ, aabb.maxX, 1 - aabb.minY, aabb.maxZ);
        } else if (facing == EnumFacing.NORTH) {
            return new AxisAlignedBB(aabb.minX, aabb.minZ, aabb.minY, aabb.maxX, aabb.maxZ, aabb.maxY);
        } else if (facing == EnumFacing.SOUTH) {
            return new AxisAlignedBB(aabb.minX, aabb.minZ, 1 - aabb.maxY, aabb.maxX, aabb.maxZ, 1 - aabb.minY);
        } else if (facing == EnumFacing.WEST) {
            return new AxisAlignedBB(aabb.minY, aabb.minZ, aabb.minX, aabb.maxY, aabb.maxZ, aabb.maxX);
        } else if (facing == EnumFacing.EAST) {
            return new AxisAlignedBB(1 - aabb.maxY, aabb.minZ, aabb.minX, 1 - aabb.minY, aabb.maxZ, aabb.maxX);
        }
        return aabb;
    }

    public static Vec3d rotateVec3d(Vec3d vec, Rotation rotation) {
        switch (rotation) {
            case NONE:
            default:
                return vec;
            case CLOCKWISE_90:
                return new Vec3d(1 - vec.zCoord, vec.yCoord, vec.xCoord);
            case CLOCKWISE_180:
                return new Vec3d(1 - vec.xCoord, vec.yCoord, 1 - vec.zCoord);
            case COUNTERCLOCKWISE_90:
                return new Vec3d(vec.zCoord, vec.yCoord, 1 - vec.xCoord);
        }
    }

    public static EnumFacing rotateAll(EnumFacing facing) {
        switch (facing) {
            case NORTH:
                return EnumFacing.EAST;
            case EAST:
                return EnumFacing.SOUTH;
            case SOUTH:
                return EnumFacing.WEST;
            case WEST:
                return EnumFacing.UP;
            case UP:
                return EnumFacing.DOWN;
            case DOWN:
                return EnumFacing.NORTH;
        }
        throw new IllegalArgumentException();
    }
}
