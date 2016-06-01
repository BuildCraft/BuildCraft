package buildcraft.core.marker;

import java.util.List;

import net.minecraft.util.math.BlockPos;

import buildcraft.lib.marker.MarkerSavedData;

public class VolumeSavedData extends MarkerSavedData<VolumeSubCache, VolumeConnection> {
    public static final String NAME = "buildcraft_marker_volume";

    public VolumeSavedData(String name) {
        super(name);
    }

    public VolumeSavedData() {
        this(NAME);
    }

    public void loadInto(VolumeSubCache subCache) {
        setCache(subCache);
        for (BlockPos p : markerPositions) {
            subCache.loadMarker(p, null);
        }
        for (List<BlockPos> list : markerConnections) {
            subCache.addConnection(new VolumeConnection(subCache, list));
        }
    }
}
