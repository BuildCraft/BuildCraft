/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.core.marker;

import buildcraft.core.client.BuildCraftLaserManager;
import buildcraft.lib.client.render.laser.LaserData_BC8;
import buildcraft.lib.client.render.laser.LaserRenderer_BC8;
import buildcraft.lib.marker.MarkerConnection;
import buildcraft.lib.marker.MarkerSubCache;
import buildcraft.lib.misc.VecUtil;
import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

public class PathConnection extends MarkerConnection<PathConnection> {
    private static final double RENDER_SCALE = 1 / 16.05;
    private static final Vec3 VEC_HALF = new Vec3(0.5, 0.5, 0.5);
    private final Deque<BlockPos> positions = new LinkedList<>();
    private boolean loop = false;

    public static boolean tryCreateConnection(PathSubCache subCache, BlockPos from, BlockPos to) {
        PathConnection connection = new PathConnection(subCache);
        connection.positions.add(from);
        connection.positions.add(to);
        subCache.addConnection(connection);
        return true;
    }

    public PathConnection(MarkerSubCache<PathConnection> subCache) {
        super(subCache);
    }

    public PathConnection(PathSubCache subCache, List<BlockPos> positions) {
        super(subCache);
        for (BlockPos p : positions) {
            if (p.equals(this.positions.peekFirst())) {
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
            loop = false;
            if (positions.size() < 2) {
                positions.clear();
            }
            subCache.refreshConnection(this);
        } else if (positions.getLast().equals(pos)) {
            positions.removeLast();
            loop = false;
            if (positions.size() < 2) {
                positions.clear();
            }
            subCache.refreshConnection(this);
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
            loop = false;
            PathConnection conA = new PathConnection(subCache);
            PathConnection conB = new PathConnection(subCache);
            conA.positions.addAll(a);
            conB.positions.addAll(b);
            positions.clear();
            subCache.destroyConnection(this);
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

    public boolean mergeWith(PathConnection conTo, BlockPos from, BlockPos to) {
        if (loop || conTo.loop) {
            return false;
        } else if (conTo == this) {
            if (positions.size() <= 2) {
                return false;
            }
            if (positions.getFirst().equals(to) && positions.getLast().equals(from)) {
                loop = true;
                subCache.refreshConnection(this);
                return true;
            } else {
                return false;
            }
        } else if (positions.getLast().equals(from) && conTo.positions.getFirst().equals(to)) {
            subCache.destroyConnection(conTo);
            positions.addAll(conTo.positions);
            subCache.refreshConnection(this);
            return true;
        } else {
            return false;
        }
    }

    public boolean canMergeWith(PathConnection conTo, BlockPos from, BlockPos to) {
        if (loop || conTo.loop) {
            return false;
        } else if (conTo == this) {
            return positions.size() > 2 && positions.getFirst().equals(to) && positions.getLast().equals(from);
        } else {
            return positions.getLast().equals(from) && conTo.positions.getFirst().equals(to);
        }
    }

    @Override
    public ImmutableList<BlockPos> getMarkerPositions() {
        if (loop && positions.size() > 0) {
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
        positions.addAll(list);
        subCache.refreshConnection(this);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void renderInWorld(PoseStack poseStack) {
        BlockPos last = null;
        for (BlockPos p : positions) {
            if (last == null) {
                last = p;
            } else {
                renderLaser(VecUtil.add(VEC_HALF, last), VecUtil.add(VEC_HALF, p), poseStack.last());
                last = p;
            }
        }
        if (loop) {
            BlockPos from = positions.getLast();
            BlockPos to = positions.getFirst();
            renderLaser(VecUtil.add(VEC_HALF, from), VecUtil.add(VEC_HALF, to), poseStack.last());
        }
    }

    @OnlyIn(Dist.CLIENT)
    private static void renderLaser(Vec3 from, Vec3 to, PoseStack.Pose pose) {
        Vec3 one = offset(from, to);
        Vec3 two = offset(to, from);
        LaserData_BC8 data = new LaserData_BC8(BuildCraftLaserManager.MARKER_PATH_CONNECTED, one, two, RENDER_SCALE);
        LaserRenderer_BC8.renderLaserStatic(data, pose);
    }

    @OnlyIn(Dist.CLIENT)
    private static Vec3 offset(Vec3 from, Vec3 to) {
        Vec3 dir = to.subtract(from).normalize();
        return from.add(VecUtil.scale(dir, 0.125));
    }
}
