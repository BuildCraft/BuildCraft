/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.core.marker;

import buildcraft.core.BCCoreConfig;
import buildcraft.core.client.BuildCraftLaserManager;
import buildcraft.lib.client.render.laser.LaserData_BC8.LaserType;
import buildcraft.lib.marker.MarkerCache;
import buildcraft.lib.marker.MarkerSubCache;
import buildcraft.lib.misc.VecUtil;
import buildcraft.lib.net.MessageMarker;
import com.google.common.collect.ImmutableList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import java.util.List;

public class PathSubCache extends MarkerSubCache<PathConnection> {
    public PathSubCache(World world) {
        super(world, MarkerCache.CACHES.indexOf(PathCache.INSTANCE));
        if (world instanceof ServerWorld) {
            ServerWorld serverLevel = (ServerWorld) world;
//            PathSavedData data = (PathSavedData) world.getPerWorldStorage().getOrLoadData(PathSavedData.class, PathSavedData.NAME);
            PathSavedData data = serverLevel.getDataStorage().get(
                    PathSavedData::new,
                    PathSavedData.NAME
            );
            if (data == null) {
                data = new PathSavedData();
                serverLevel.getDataStorage().set(data);
            }
            data.loadInto(this);
        }
    }

    @Override
    public boolean tryConnect(BlockPos from, BlockPos to) {
        PathConnection conFrom = getConnection(from);
        PathConnection conTo = getConnection(to);
        if (conFrom == null) {
            if (conTo == null) {
                return PathConnection.tryCreateConnection(this, from, to);
            } else {
                return conTo.addMarker(from, to);
            }
        } else {
            if (conTo == null) {
                return conFrom.addMarker(from, to);
            } else {
                return conFrom.mergeWith(conTo, from, to);
            }
        }
    }

    @Override
    public boolean canConnect(BlockPos from, BlockPos to) {
        PathConnection conFrom = getConnection(from);
        PathConnection conTo = getConnection(to);
        if (conFrom == null) {
            if (conTo == null) {
                return true;
            } else {
                return conTo.canAddMarker(from, to);
            }
        } else {
            if (conTo == null) {
                return conFrom.canAddMarker(from, to);
            } else {
                return conFrom.canMergeWith(conTo, from, to);
            }
        }
    }

    @Override
    public ImmutableList<BlockPos> getValidConnections(BlockPos from) {
        ImmutableList.Builder<BlockPos> list = ImmutableList.builder();
        final int maxLengthSquared = BCCoreConfig.markerMaxDistance * BCCoreConfig.markerMaxDistance;
        for (BlockPos pos : getAllMarkers()) {
            if (pos.equals(from)) {
                continue;
            }
            if (VecUtil.distanceSq(pos, from) > maxLengthSquared) {
                continue;
            }
            if (canConnect(from, pos) || canConnect(pos, from)) {
                list.add(pos);
            }
        }
        return list.build();
    }

    @Override
    public LaserType getPossibleLaserType() {
        return BuildCraftLaserManager.MARKER_PATH_POSSIBLE;
    }

    @Override
    protected boolean handleMessage(MessageMarker message) {
        List<BlockPos> positions = message.positions;
        if (message.connection) {
            if (message.add) {
                for (BlockPos p : positions) {
                    PathConnection existing = this.getConnection(p);
                    destroyConnection(existing);
                }
                PathConnection con = new PathConnection(this, positions);
                addConnection(con);
            } else { // removing from a connection
                for (BlockPos p : positions) {
                    PathConnection existing = this.getConnection(p);
                    if (existing != null) {
                        existing.removeMarker(p);
                        refreshConnection(existing);
                    }
                }
            }
        }
        return false;
    }
}
