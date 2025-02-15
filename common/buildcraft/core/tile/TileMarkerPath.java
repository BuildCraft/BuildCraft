/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.core.tile;

import buildcraft.api.core.IPathProvider;
import buildcraft.core.BCCoreBlocks;
import buildcraft.core.marker.PathCache;
import buildcraft.core.marker.PathConnection;
import buildcraft.lib.tile.TileMarker;
import com.google.common.collect.ImmutableList;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class TileMarkerPath extends TileMarker<PathConnection> implements IPathProvider {

    public TileMarkerPath(BlockPos pos, BlockState blockState) {
        super(BCCoreBlocks.markerPathTile.get(), pos, blockState);
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
    public void removeFromWorld() {
        for (BlockPos pos : getPath()) {
//            world.destroyBlock(pos, true);
            level.destroyBlock(pos, /*pDropBlock*/ true);
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
