package buildcraft.core.tile;

import java.util.*;

import net.minecraft.util.math.BlockPos;

import buildcraft.api.core.IPathProvider;
import buildcraft.lib.tile.TileMarkerBase;

public class TileMarkerPath extends TileMarkerBase<TileMarkerPath> implements IPathProvider {
    public static final Map<BlockPos, TileMarkerPath> PATH_CACHE = new HashMap<>();
    /** The path marker that comes before this. Used to dictate the direction of this path. Will be null if its not
     * connected to anything. */
    private BlockPos from, to;

    @Override
    protected TileMarkerPath getAsType() {
        return this;
    }

    @Override
    public Map<BlockPos, TileMarkerPath> getCache() {
        return PATH_CACHE;
    }

    @Override
    public boolean isActiveForRender() {
        return false;
    }

    @Override
    public boolean canConnectTo(TileMarkerPath other) {
        if (allConnected.size() >= 2) return false;
        if (from == null && other.to == null) return true;
        if (to == null && other.from == null) return true;
        return false;
    }

    @Override
    protected void onConnect(TileMarkerPath other) {
        if (to == null && other.from == null) {
            // Setup both variables so we don't screw anything up by doing them indervidually
            to = other.getPos();
            other.from = getPos();
        } else if (from == null && other.to == null) {
            from = other.getPos();
            other.to = getPos();
        }
    }

    @Override
    protected void onDisconnect(TileMarkerPath other) {
        if (other.getPos().equals(to)) {
            to = null;
            other.from = null;
        } else if (other.getPos().equals(from)) {
            from = null;
            other.to = null;
        }
    }

    @Override
    public List<BlockPos> getPath() {
        Set<TileMarkerPath> visited = new HashSet<>();
        List<BlockPos> positions = new ArrayList<>();
        // Find the first one with a null "from", or its already been visited
        BlockPos first = getPos();
        TileMarkerPath start = this;
        while (first != null && !visited.contains(start)) {
            visited.add(start);
            TileMarkerPath path = PATH_CACHE.get(first);
            if (path == null) break;
            start = path;
            first = start.from;
        }
        // Now iterate through all of the set, removing each one as its added to the list
        visited.clear();
        visited.add(start);
        positions.add(first);
        while (start != null && start.to != null && !visited.contains(start)) {
            visited.add(start);
            positions.add(start.to);
            start = PATH_CACHE.get(start.to);
        }
        return positions;
    }

    @Override
    public void removeFromWorld() {
        if (worldObj.isRemote) return;
        for (TileMarkerPath connectedTo : gatherAllConnections()) {
            worldObj.destroyBlock(connectedTo.getPos(), true);
        }
    }
}
