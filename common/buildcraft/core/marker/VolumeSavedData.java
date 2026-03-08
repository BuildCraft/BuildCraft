/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.core.marker;

import buildcraft.lib.marker.MarkerSavedData;
import net.minecraft.util.math.BlockPos;

import java.util.List;

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
