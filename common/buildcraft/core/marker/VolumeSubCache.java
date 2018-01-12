/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.core.marker;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableList;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.lib.client.render.laser.LaserData_BC8.LaserType;
import buildcraft.lib.marker.MarkerCache;
import buildcraft.lib.marker.MarkerSubCache;
import buildcraft.lib.net.MessageMarker;

import buildcraft.core.BCCoreConfig;
import buildcraft.core.client.BuildCraftLaserManager;

public class VolumeSubCache extends MarkerSubCache<VolumeConnection> {
    public VolumeSubCache(World world) {
        super(world, MarkerCache.CACHES.indexOf(VolumeCache.INSTANCE));
        VolumeSavedData data = (VolumeSavedData) world.getPerWorldStorage().getOrLoadData(VolumeSavedData.class, VolumeSavedData.NAME);
        if (data == null) {
            data = new VolumeSavedData();
            world.getPerWorldStorage().setData(VolumeSavedData.NAME, data);
        }
        data.loadInto(this);
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
        Set<Axis> taken = EnumSet.noneOf(EnumFacing.Axis.class);
        if (existing != null) {
            taken.addAll(existing.getConnectedAxis());
        }

        ImmutableList.Builder<BlockPos> valids = ImmutableList.builder();
        for (EnumFacing face : EnumFacing.VALUES) {
            if (taken.contains(face.getAxis())) continue;
            for (int i = 1; i <= BCCoreConfig.markerMaxDistance; i++) {
                BlockPos toTry = from.offset(face, i);
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
    @SideOnly(Side.CLIENT)
    public LaserType getPossibleLaserType() {
        return BuildCraftLaserManager.MARKER_VOLUME_POSSIBLE;
    }

    @Override
    @SideOnly(Side.CLIENT)
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
