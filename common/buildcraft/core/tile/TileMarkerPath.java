package buildcraft.core.tile;

import com.google.common.collect.ImmutableList;

import net.minecraft.util.math.BlockPos;

import buildcraft.api.core.IPathProvider;
import buildcraft.core.marker.PathCache;
import buildcraft.core.marker.PathConnection;
import buildcraft.lib.tile.TileMarker;

public class TileMarkerPath extends TileMarker<PathConnection> implements IPathProvider {

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
            worldObj.destroyBlock(pos, true);
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
        if (worldObj.isRemote) {
            return;
        }
        PathConnection connection = getCurrentConnection();
        if (connection == null) {
            return;
        }
        connection.reverseDirection();
    }
}
