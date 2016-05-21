package buildcraft.core.marker;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.core.BCCoreConfig;
import buildcraft.core.Box;
import buildcraft.core.client.BuildCraftLaserManager;
import buildcraft.core.marker.VolumeCache.SubCacheVolume;
import buildcraft.lib.client.render.LaserData_BC8;
import buildcraft.lib.client.render.LaserData_BC8.LaserType;
import buildcraft.lib.client.render.LaserRenderer_BC8;
import buildcraft.lib.marker.MarkerConnection2;
import buildcraft.lib.misc.PositionUtil;

public class VolumeConnection extends MarkerConnection2<VolumeConnection> {
    private static final double RENDER_SCALE = 1 / 16.05;
    private static final LaserType LASER_TYPE = BuildCraftLaserManager.MARKER_VOLUME_CONNECTED;
    private static final Vec3d VEC_HALF = new Vec3d(0.5, 0.5, 0.5);

    private final Set<BlockPos> makup = new HashSet<>();
    private final Box box = new Box();

    public static boolean tryCreateConnection(SubCacheVolume subCache, BlockPos from, BlockPos to) {
        if (canCreateConnection(subCache, from, to)) {
            VolumeConnection connection = new VolumeConnection(subCache);
            connection.makup.add(from);
            connection.makup.add(to);
            connection.createBox();
            subCache.addConnection(connection);
            return true;
        }
        return false;
    }

    public static boolean canCreateConnection(SubCacheVolume subCache, BlockPos from, BlockPos to) {
        EnumFacing directOffset = PositionUtil.getDirectFacingOffset(from, to);
        if (directOffset == null) return false;
        for (int i = 1; i < BCCoreConfig.markerMaxDistance; i++) {
            BlockPos offset = from.offset(directOffset, i);
            if (offset.equals(to)) return true;
            if (subCache.hasLoadedOrUnloadedMarker(offset)) return false;
        }
        return false;
    }

    public VolumeConnection(SubCacheVolume subCache) {
        super(subCache);
    }

    @Override
    public void removeMarker(BlockPos pos) {
        makup.remove(pos);
        createBox();
    }

    public boolean addMarker(BlockPos pos) {
        if (canAddMarker(pos)) {
            makup.add(pos);
            createBox();
            subCache.refreshConnection(this);
            return true;
        }
        return false;
    }

    public boolean canAddMarker(BlockPos to) {
        for (BlockPos from : makup) {

        }
        // Check validity
        return true;
    }

    public boolean mergeWith(VolumeConnection other) {
        if (canMergeWith(other)) {
            // Merge
        }
        return false;
    }

    public boolean canMergeWith(VolumeConnection other) {
        // Check validity
        return false;
    }

    @Override
    public Collection<BlockPos> getMarkerPositions() {
        return makup;
    }

    private void createBox() {
        box.reset();
        for (BlockPos p : makup) {
            box.extendToEncompass(p);
        }
    }

    public Box getBox() {
        return new Box(box.min(), box.max());
    }

    // ###########
    //
    // Rendering
    //
    // ###########

    @Override
    @SideOnly(Side.CLIENT)
    public void renderInWorld() {
        if (box.min() == null || box.max() == null) return;

        int sizeX = box.size().getX();
        int sizeY = box.size().getY();
        int sizeZ = box.size().getZ();

        BlockPos min = box.min();
        BlockPos max = box.max();

        Vec3d[][][] vecs = new Vec3d[2][2][2];
        vecs[0][0][0] = new Vec3d(min).add(VEC_HALF);
        vecs[1][0][0] = new Vec3d(new BlockPos(max.getX(), min.getY(), min.getZ())).add(VEC_HALF);
        vecs[0][1][0] = new Vec3d(new BlockPos(min.getX(), max.getY(), min.getZ())).add(VEC_HALF);
        vecs[1][1][0] = new Vec3d(new BlockPos(max.getX(), max.getY(), min.getZ())).add(VEC_HALF);
        vecs[0][0][1] = new Vec3d(new BlockPos(min.getX(), min.getY(), max.getZ())).add(VEC_HALF);
        vecs[1][0][1] = new Vec3d(new BlockPos(max.getX(), min.getY(), max.getZ())).add(VEC_HALF);
        vecs[0][1][1] = new Vec3d(new BlockPos(min.getX(), max.getY(), max.getZ())).add(VEC_HALF);
        vecs[1][1][1] = new Vec3d(max).add(VEC_HALF);

        if (sizeX > 1) {
            renderLaser(vecs[0][0][0], vecs[1][0][0], Axis.X);
            if (sizeY > 1) {
                renderLaser(vecs[0][1][0], vecs[1][1][0], Axis.X);
                if (sizeZ > 1) {
                    renderLaser(vecs[0][1][1], vecs[1][1][1], Axis.X);
                }
            }
            if (sizeZ > 1) {
                renderLaser(vecs[0][0][1], vecs[1][0][1], Axis.X);
            }
        }

        if (sizeY > 1) {
            renderLaser(vecs[0][0][0], vecs[0][1][0], Axis.Y);
            if (sizeX > 1) {
                renderLaser(vecs[1][0][0], vecs[1][1][0], Axis.Y);
                if (sizeZ > 1) {
                    renderLaser(vecs[1][0][1], vecs[1][1][1], Axis.Y);
                }
            }
            if (sizeZ > 1) {
                renderLaser(vecs[0][0][1], vecs[0][1][1], Axis.Y);
            }
        }

        if (box.size().getZ() > 1) {
            renderLaser(vecs[0][0][0], vecs[0][0][1], Axis.Z);
            if (sizeX > 1) {
                renderLaser(vecs[1][0][0], vecs[1][0][1], Axis.Z);
                if (sizeY > 0) {
                    renderLaser(vecs[1][1][0], vecs[1][1][1], Axis.Z);
                }
            }
            if (sizeY > 0) {
                renderLaser(vecs[0][1][0], vecs[0][1][1], Axis.Z);
            }
        }
    }

    private static void renderLaser(Vec3d min, Vec3d max, Axis axis) {
        EnumFacing faceForMin = PositionUtil.getFacing(axis, true);
        EnumFacing faceForMax = PositionUtil.getFacing(axis, false);
        Vec3d one = offset(min, faceForMin);
        Vec3d two = offset(max, faceForMax);
        LaserData_BC8 data = new LaserData_BC8(LASER_TYPE, one, two, RENDER_SCALE);
        LaserRenderer_BC8.renderLaser(data);
    }

    private static Vec3d offset(Vec3d vec, EnumFacing face) {
        double by = 1 / 16.0;
        if (face == EnumFacing.DOWN) {
            return vec.addVector(0, -by, 0);
        } else if (face == EnumFacing.UP) {
            return vec.addVector(0, by, 0);
        } else if (face == EnumFacing.EAST) {
            return vec.addVector(by, 0, 0);
        } else if (face == EnumFacing.WEST) {
            return vec.addVector(-by, 0, 0);
        } else if (face == EnumFacing.SOUTH) {
            return vec.addVector(0, 0, by);
        } else {// North
            return vec.addVector(0, 0, -by);
        }
    }
}
