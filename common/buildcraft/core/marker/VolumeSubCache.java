/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.core.marker;

import buildcraft.core.BCCoreConfig;
import buildcraft.core.client.BuildCraftLaserManager;
import buildcraft.lib.client.render.laser.LaserData_BC8;
import buildcraft.lib.marker.MarkerCache;
import buildcraft.lib.marker.MarkerSubCache;
import buildcraft.lib.net.MessageMarker;
import com.google.common.collect.ImmutableList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class VolumeSubCache extends MarkerSubCache<VolumeConnection> {
    public VolumeSubCache(Level world) {
        super(world, MarkerCache.CACHES.indexOf(VolumeCache.INSTANCE));
        if (world instanceof ServerLevel serverLevel) {
//            VolumeSavedData data = (VolumeSavedData) world.getPerWorldStorage().getOrLoadData(VolumeSavedData.class, VolumeSavedData.NAME);
            VolumeSavedData data = (VolumeSavedData) serverLevel.getDataStorage().get(
                    (nbt) ->
                    {
                        VolumeSavedData ret = new VolumeSavedData();
                        ret.readFromNBT(nbt);
                        return ret;
                    },
                    VolumeSavedData.NAME
            );
            if (data == null) {
                data = new VolumeSavedData();
                serverLevel.getDataStorage().set(VolumeSavedData.NAME, data);
            }
            data.loadInto(this);
        }
    }

    @Override
    public boolean tryConnect(BlockPos from, BlockPos to) {
        VolumeConnection fromConnection = getConnection(from);
        VolumeConnection toConnection = getConnection(to);
        if (fromConnection == null) {
            if (toConnection == null) {
                return VolumeConnection.tryCreateConnection(this, from, to);
            } else {// The other one has a connection
                return toConnection.addMarker(from);
            }
        } else {// We have a connection
            if (toConnection == null) {
                return fromConnection.addMarker(to);
            } else {// The other one has a connection
                return fromConnection.mergeWith(toConnection);
            }
        }
    }

    @Override
    public boolean canConnect(BlockPos from, BlockPos to) {
        VolumeConnection fromConnection = getConnection(from);
        VolumeConnection toConnection = getConnection(to);
        if (fromConnection == null) {
            if (toConnection == null) {
                return VolumeConnection.canCreateConnection(this, from, to);
            } else {// The other one has a connection
                return toConnection.canAddMarker(from);
            }
        } else {// We have a connection
            if (toConnection == null) {
                return fromConnection.canAddMarker(to);
            } else {// The other one has a connection
                return fromConnection.canMergeWith(toConnection);
            }
        }
    }

    @Override
    public ImmutableList<BlockPos> getValidConnections(BlockPos from) {
        VolumeConnection existing = getConnection(from);
        Set<Direction.Axis> taken = EnumSet.noneOf(Direction.Axis.class);
        if (existing != null) {
            taken.addAll(existing.getConnectedAxis());
        }

        ImmutableList.Builder<BlockPos> valids = ImmutableList.builder();
        for (Direction face : Direction.values()) {
            if (taken.contains(face.getAxis())) continue;
            for (int i = 1; i <= BCCoreConfig.markerMaxDistance; i++) {
                BlockPos toTry = from.relative(face, i);
                if (hasLoadedOrUnloadedMarker(toTry)) {
                    if (!canConnect(from, toTry)) break;
                    valids.add(toTry);
                    break;
                }
            }
        }
        return valids.build();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public LaserData_BC8.LaserType getPossibleLaserType() {
        return BuildCraftLaserManager.MARKER_VOLUME_POSSIBLE;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    protected boolean handleMessage(MessageMarker message) {
        List<BlockPos> positions = message.positions;
        if (message.connection) {
            if (message.add) {
                for (BlockPos p : positions) {
                    VolumeConnection existing = this.getConnection(p);
                    destroyConnection(existing);
                }
                VolumeConnection con = new VolumeConnection(this, positions);
                addConnection(con);
            } else { // removing from a connection
                for (BlockPos p : positions) {
                    VolumeConnection existing = this.getConnection(p);
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
