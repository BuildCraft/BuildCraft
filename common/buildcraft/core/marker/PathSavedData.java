package buildcraft.core.marker;

import java.util.List;

import net.minecraft.util.math.BlockPos;

import buildcraft.lib.marker.MarkerSavedData;

public class PathSavedData extends MarkerSavedData<PathSubCache, PathConnection> {
    public static final String NAME = "buildcraft_marker_path";

    public PathSavedData(String name) {
        super(name);
    }

    public PathSavedData() {
        this(NAME);
    }

    public void loadInto(PathSubCache subCache) {
        setCache(subCache);
        for (BlockPos p : markerPositions) {
            subCache.loadMarker(p, null);
        }
        for (List<BlockPos> list : markerConnections) {
            subCache.addConnection(new PathConnection(subCache, list));
        }
    }
}
