package buildcraft.core.marker;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import com.google.common.collect.ImmutableList;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.core.client.BuildCraftLaserManager;
import buildcraft.core.marker.PathCache.SubCachePath;
import buildcraft.lib.client.render.LaserData_BC8;
import buildcraft.lib.client.render.LaserRenderer_BC8;
import buildcraft.lib.marker.MarkerCache.SubCache;
import buildcraft.lib.marker.MarkerConnection;
import buildcraft.lib.misc.VecUtil;

public class PathConnection extends MarkerConnection<PathConnection> {
    private static final double RENDER_SCALE = 1 / 16.05;
    private static final Vec3d VEC_HALF = new Vec3d(0.5, 0.5, 0.5);
    private final Deque<BlockPos> positions = new LinkedList<>();
    private boolean loop = false;

    public static boolean tryCreateConnection(SubCachePath subCache, BlockPos from, BlockPos to) {
        if (canCreateConnection(subCache, from, to)) {
            PathConnection connection = new PathConnection(subCache);
            connection.positions.add(from);
            connection.positions.add(to);
            subCache.addConnection(connection);
            return true;
        }
        return false;
    }

    public static boolean canCreateConnection(SubCachePath subCache, BlockPos from, BlockPos to) {
        return true;
    }

    public PathConnection(SubCache<PathConnection> subCache) {
        super(subCache);
    }

    public PathConnection(SubCachePath subCache, List<BlockPos> positions) {
        super(subCache);
        for (BlockPos p : positions) {
            if (p.equals(this.positions.peekLast())) {
                loop = true;
                break;
            } else {
                this.positions.addLast(p);
            }
        }
    }

    @Override
    public void removeMarker(BlockPos pos) {
        if (positions.getFirst().equals(pos)) {
            positions.removeFirst();
            if (positions.size() < 2) {
                positions.clear();
            }
        } else if (positions.getLast().equals(pos)) {
            positions.removeLast();
            if (positions.size() < 2) {
                positions.clear();
            }
        } else if (positions.contains(pos)) {
            List<BlockPos> a = new ArrayList<>();
            List<BlockPos> b = new ArrayList<>();
            boolean hasReached = false;
            for (BlockPos p : positions) {
                if (p.equals(pos)) {
                    hasReached = true;
                } else if (hasReached) {
                    b.add(p);
                } else {
                    a.add(p);
                }
            }
            PathConnection conA = new PathConnection(subCache);
            PathConnection conB = new PathConnection(subCache);
            conA.positions.addAll(a);
            conB.positions.addAll(b);
            positions.clear();
            subCache.refreshConnection(this);
            subCache.addConnection(conA);
            subCache.addConnection(conB);
        }
    }

    public boolean addMarker(BlockPos from, BlockPos toAdd) {
        if (loop) {
            return false;
        }
        boolean contains = positions.contains(toAdd);
        if (positions.getFirst().equals(from)) {
            if (positions.getLast().equals(toAdd)) {
                loop = true;
            } else if (!contains) {
                positions.addFirst(toAdd);
            } else {
                return false;
            }
            subCache.refreshConnection(this);
            return true;
        } else if (positions.getLast().equals(from)) {
            if (positions.getFirst().equals(toAdd)) {
                loop = true;
                return true;
            } else if (!contains) {
                positions.addLast(toAdd);
            } else {
                return false;
            }
            subCache.refreshConnection(this);
            return true;
        } else {
            return false;
        }
    }

    public boolean canAddMarker(BlockPos from, BlockPos toAdd) {
        if (loop) {
            return false;
        }
        boolean contains = positions.contains(toAdd);
        if (positions.getFirst().equals(from)) {
            if (contains) {
                return positions.getLast().equals(toAdd);
            } else {
                return true;
            }
        } else if (positions.getLast().equals(from)) {
            if (contains) {
                return positions.getLast().equals(toAdd);
            } else {
                return true;
            }
        } else {
            return false;
        }
    }

    @Override
    public ImmutableList<BlockPos> getMarkerPositions() {
        if (loop) {
            ImmutableList.Builder<BlockPos> list = ImmutableList.builder();
            list.addAll(positions);
            list.add(positions.getFirst());
            return list.build();
        }
        return ImmutableList.copyOf(positions);
    }

    public void reverseDirection() {
        Deque<BlockPos> list = new LinkedList<>();
        while (!positions.isEmpty()) {
            list.addFirst(positions.removeFirst());
        }
        positions.clear();
        for (BlockPos pos : list) {
            positions.add(pos);
        }
        subCache.refreshConnection(this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderInWorld() {
        BlockPos last = null;
        for (BlockPos p : positions) {
            if (last == null) {
                last = p;
            } else {
                renderLaser(VecUtil.add(VEC_HALF, last), VecUtil.add(VEC_HALF, p));
                last = p;
            }
        }
        if (loop) {
            BlockPos from = positions.getLast();
            BlockPos to = positions.getFirst();
            renderLaser(VecUtil.add(VEC_HALF, from), VecUtil.add(VEC_HALF, to));
        }
    }

    @SideOnly(Side.CLIENT)
    private static void renderLaser(Vec3d from, Vec3d to) {
        Vec3d one = offset(from, to);
        Vec3d two = offset(to, from);
        LaserData_BC8 data = new LaserData_BC8(BuildCraftLaserManager.MARKER_PATH_CONNECTED, one, two, RENDER_SCALE);
        LaserRenderer_BC8.renderLaser(data);
    }

    @SideOnly(Side.CLIENT)
    private static Vec3d offset(Vec3d from, Vec3d to) {
        Vec3d dir = to.subtract(from).normalize();
        return from.add(VecUtil.scale(dir, 0.125));
    }
}
