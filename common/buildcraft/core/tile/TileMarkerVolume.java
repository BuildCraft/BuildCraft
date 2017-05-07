/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.core.tile;

import java.io.IOException;
import java.util.List;

import com.google.common.collect.ImmutableList;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.tiles.ITileAreaProvider;
import buildcraft.api.tiles.TilesAPI;

import buildcraft.core.BCCoreConfig;
import buildcraft.core.marker.VolumeCache;
import buildcraft.core.marker.VolumeConnection;
import buildcraft.lib.marker.MarkerSubCache;
import buildcraft.lib.misc.PermissionUtil;
import buildcraft.lib.misc.PositionUtil;
import buildcraft.lib.misc.data.Box;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.lib.tile.TileMarker;

public class TileMarkerVolume extends TileMarker<VolumeConnection> implements ITileAreaProvider {
    public static final int NET_SIGNALS_ON = 10;
    public static final int NET_SIGNALS_OFF = 11;

    private boolean showSignals = false;

    public TileMarkerVolume() {
        caps.addCapabilityInstance(TilesAPI.CAP_TILE_AREA_PROVIDER, this, EnumPipePart.VALUES);
    }

    public boolean isShowingSignals() {
        return showSignals;
    }

    @Override
    public VolumeCache getCache() {
        return VolumeCache.INSTANCE;
    }

    @Override
    public boolean isActiveForRender() {
        return showSignals || getCurrentConnection() != null;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setBoolean("showSignals", showSignals);
        return nbt;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        showSignals = nbt.getBoolean("showSignals");
    }

    public void switchSignals() {
        if (!world.isRemote) {
            showSignals = !showSignals;
            markDirty();
            sendNetworkUpdate(showSignals ? NET_SIGNALS_ON : NET_SIGNALS_OFF);
        }
    }

    private void readNewSignalState(boolean shouldShow) {
        boolean before = isActiveForRender();
        showSignals = shouldShow;
        if (before != isActiveForRender()) {
            redrawBlock();
        }
    }

    @Override
    public void writePayload(int id, PacketBufferBC buffer, Side side) {
        super.writePayload(id, buffer, side);
        if (side == Side.SERVER) {
            if (id == NET_RENDER_DATA) {
                buffer.writeBoolean(showSignals);
            }
        }
    }

    @Override
    public void readPayload(int id, PacketBufferBC buffer, Side side, MessageContext ctx) throws IOException {
        super.readPayload(id, buffer, side, ctx);
        if (side == Side.CLIENT) {
            if (id == NET_SIGNALS_ON) {
                readNewSignalState(true);
            } else if (id == NET_SIGNALS_OFF) {
                readNewSignalState(false);
            } else if (id == NET_RENDER_DATA) {
                readNewSignalState(buffer.readBoolean());
            }
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        return INFINITE_EXTENT_AABB;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public double getMaxRenderDistanceSquared() {
        return BCCoreConfig.markerMaxDistance * 4 * BCCoreConfig.markerMaxDistance;
    }

    public void onManualConnectionAttempt(EntityPlayer player) {
        if (PermissionUtil.hasPermission(PermissionUtil.PERM_EDIT, player, getPermBlock())) {
            MarkerSubCache<VolumeConnection> cache = this.getLocalCache();
            for (BlockPos other : cache.getValidConnections(getPos())) {
                TileMarkerVolume tile = (TileMarkerVolume) cache.getMarker(other);
                if (tile == null) continue;
                if (PermissionUtil.hasPermission(PermissionUtil.PERM_EDIT, player, getPermBlock())) {
                    cache.tryConnect(getPos(), other);
                }
            }
        }
    }

    @Override
    public void getDebugInfo(List<String> left, List<String> right, EnumFacing side) {
        super.getDebugInfo(left, right, side);
        left.add("");
        left.add("Min = " + min());
        left.add("Max = " + max());
        left.add("Signals = " + showSignals);
    }

    // ITileAreaProvider

    @Override
    public BlockPos min() {
        VolumeConnection connection = getCurrentConnection();
        return connection == null ? getPos() : connection.getBox().min();
    }

    @Override
    public BlockPos max() {
        VolumeConnection connection = getCurrentConnection();
        return connection == null ? getPos() : connection.getBox().max();
    }

    @Override
    public void removeFromWorld() {
        if (world.isRemote) {
            return;
        }
        VolumeConnection connection = getCurrentConnection();
        if (connection != null) {
            // Copy the list over because the iterator doesn't like it if you change the connection while using it
            List<BlockPos> allPositions = ImmutableList.copyOf(connection.getMarkerPositions());
            for (BlockPos p : allPositions) {
                world.destroyBlock(p, true);
            }
        }
    }

    @Override
    public boolean isValidFromLocation(BlockPos pos) {
        VolumeConnection connection = getCurrentConnection();
        if (connection == null) {
            return false;
        }
        Box box = connection.getBox();
        if (box.contains(pos)) {
            return false;
        }
        for (BlockPos p : PositionUtil.getCorners(box.min(), box.max())) {
            if (PositionUtil.isNextTo(p, pos)) {
                return true;
            }
        }
        return false;
    }
}
