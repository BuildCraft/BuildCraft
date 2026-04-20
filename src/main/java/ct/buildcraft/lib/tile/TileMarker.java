/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.lib.tile;

import java.util.List;

import ct.buildcraft.api.tiles.IDebuggable;
import ct.buildcraft.lib.marker.MarkerCache;
import ct.buildcraft.lib.marker.MarkerConnection;
import ct.buildcraft.lib.marker.MarkerSubCache;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class TileMarker<C extends MarkerConnection<C>> extends TileBC_Neptune implements IDebuggable {
	
    public TileMarker(BlockEntityType<?> p_155228_, BlockPos p_155229_, BlockState p_155230_) {
		super(p_155228_, p_155229_, p_155230_);
	}

	public abstract MarkerCache<? extends MarkerSubCache<C>> getCache();

    public MarkerSubCache<C> getLocalCache() {
        return getCache().getSubCache(level);
    }

    /** @return True if this has lasers being emitted, or any other reason you want. Activates the surrounding "glow"
     *         parts for the block model. */
    public abstract boolean isActiveForRender();

    public C getCurrentConnection() {
        return getLocalCache().getConnection(getBlockPos());
    }

    @Override
    public void onLoad() {
        super.onLoad();
        getLocalCache().loadMarker(getBlockPos(), this);
    }

	@Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        getLocalCache().unloadMarker(getBlockPos());
    }
    
    @Override
	public void setRemoved() {
		super.setRemoved();
        getLocalCache().removeMarker(getBlockPos());
	}

    @Override
    public void onRemove(boolean dropSelf) {
        super.onRemove(dropSelf);
        getLocalCache().removeMarker(getBlockPos());
    }

    protected void disconnectFromOthers() {
        C currentConnection = getCurrentConnection();
        if (currentConnection != null) {
            currentConnection.removeMarker(getBlockPos());
        }
    }

    @Override
    public void getDebugInfo(List<String> left, List<String> right, Direction side) {
        C current = getCurrentConnection();
        MarkerSubCache<C> cache = getLocalCache();
        left.add("Exists = " + (cache.getMarker(getBlockPos()) == this));
        if (current == null) {
            left.add("Connection = null");
        } else {
            left.add("Connection:");
            current.getDebugInfo(getBlockPos(), left);
        }
    }
}
