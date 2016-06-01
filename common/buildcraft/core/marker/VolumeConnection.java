package buildcraft.core.marker;

import java.util.Collection;
import java.util.EnumSet;
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
import buildcraft.lib.client.render.LaserData_BC8;
import buildcraft.lib.client.render.LaserRenderer_BC8;
import buildcraft.lib.marker.MarkerConnection;
import buildcraft.lib.misc.PositionUtil;
import buildcraft.lib.misc.VecUtil;

public class VolumeConnection extends MarkerConnection<VolumeConnection> {
    private static final double RENDER_SCALE = 1 / 16.05;
    private static final Vec3d VEC_HALF = new Vec3d(0.5, 0.5, 0.5);

    private final Set<BlockPos> makup = new HashSet<>();
    private final Box box = new Box();

    public static boolean tryCreateConnection(VolumeSubCache subCache, BlockPos from, BlockPos to) {
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

    public static boolean canCreateConnection(VolumeSubCache subCache, BlockPos from, BlockPos to) {
        EnumFacing directOffset = PositionUtil.getDirectFacingOffset(from, to);
        if (directOffset == null) return false;
        for (int i = 1; i < BCCoreConfig.markerMaxDistance; i++) {
            BlockPos offset = from.offset(directOffset, i);
            if (offset.equals(to)) return true;
            if (subCache.hasLoadedOrUnloadedMarker(offset)) return false;
        }
        return false;
    }

    public VolumeConnection(VolumeSubCache subCache) {
        super(subCache);
    }

    public VolumeConnection(VolumeSubCache subCache, Collection<BlockPos> positions) {
        super(subCache);
        makup.addAll(positions);
        createBox();
    }

    @Override
    public void removeMarker(BlockPos pos) {
        makup.remove(pos);
        if (makup.size() < 2) {
            // This connection will be removed by the sub-cache
            makup.clear();
        }
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
        Set<Axis> taken = getConnectedAxis();
        for (BlockPos from : makup) {
            EnumFacing direct = PositionUtil.getDirectFacingOffset(from, to);
            if (direct != null && !taken.contains(direct.getAxis())) {
                return true;
            }
        }
        return false;
    }

    public boolean mergeWith(VolumeConnection other) {
        if (canMergeWith(other)) {
            makup.addAll(other.makup);
            other.makup.clear();
            createBox();
            subCache.refreshConnection(other);
            subCache.refreshConnection(this);
            return true;
        }
        return false;
    }

    public boolean canMergeWith(VolumeConnection other) {
        EnumSet<Axis> us = getConnectedAxis();
        EnumSet<Axis> them = other.getConnectedAxis();
        if (us.size() != 1 || them.size() != 1) {
            return false;
        }
        if (us.equals(them)) {
            return false;
        }
        Set<Axis> blacklisted = EnumSet.copyOf(us);
        blacklisted.addAll(them);
        for (BlockPos from : makup) {
            for (BlockPos to : other.makup) {
                EnumFacing offset = PositionUtil.getDirectFacingOffset(from, to);
                if (offset != null && !blacklisted.contains(offset.getAxis())) {
                    return true;
                }
            }
        }
        return false;
    }

    public EnumSet<Axis> getConnectedAxis() {
        EnumSet<Axis> taken = EnumSet.noneOf(EnumFacing.Axis.class);
        for (BlockPos a : getMarkerPositions()) {
            for (BlockPos b : getMarkerPositions()) {
                EnumFacing offset = PositionUtil.getDirectFacingOffset(a, b);
                if (offset != null) {
                    taken.add(offset.getAxis());
                }
            }
        }
        return taken;
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

    @SideOnly(Side.CLIENT)
    private static void renderLaser(Vec3d min, Vec3d max, Axis axis) {
        EnumFacing faceForMin = VecUtil.getFacing(axis, true);
        EnumFacing faceForMax = VecUtil.getFacing(axis, false);
        Vec3d one = offset(min, faceForMin);
        Vec3d two = offset(max, faceForMax);
        LaserData_BC8 data = new LaserData_BC8(BuildCraftLaserManager.MARKER_VOLUME_CONNECTED, one, two, RENDER_SCALE);
        LaserRenderer_BC8.renderLaser(data);
    }

    @SideOnly(Side.CLIENT)
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
