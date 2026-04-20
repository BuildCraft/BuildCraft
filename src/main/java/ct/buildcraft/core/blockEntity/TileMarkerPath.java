/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package ct.buildcraft.core.blockEntity;

import com.google.common.collect.ImmutableList;

import ct.buildcraft.api.core.IPathProvider;
import ct.buildcraft.core.BCCoreBlocks;
import ct.buildcraft.core.marker.PathCache;
import ct.buildcraft.core.marker.PathConnection;
import ct.buildcraft.lib.tile.TileMarker;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

public class TileMarkerPath extends TileMarker<PathConnection> implements IPathProvider {

    public TileMarkerPath(BlockPos pos, BlockState state) {
		super(BCCoreBlocks.MARKER_PATH_TILE_BC8.get(), pos, state);
	}

	@Override
    public ImmutableList<BlockPos> getPath() {
        PathConnection connection = getCurrentConnection();
        if (connection == null) {
            return ImmutableList.of();
        }
        return connection.getMarkerPositions();
    }

    @Override
    public void removeFromWorld(Player player) {
    	boolean shouldDrop = player == null || !player.isCreative();
        for (BlockPos pos : getPath()) {
            level.destroyBlock(pos, shouldDrop);
        }
    }

    @Override
    public PathCache getCache() {
        return PathCache.INSTANCE;
    }

    @Override
    public boolean isActiveForRender() {
        PathConnection connection = getCurrentConnection();
        return connection != null;
    }

    public void reverseDirection() {
        if (level.isClientSide) {
            return;
        }
        PathConnection connection = getCurrentConnection();
        if (connection == null) {
            return;
        }
        connection.reverseDirection();
    }
}
