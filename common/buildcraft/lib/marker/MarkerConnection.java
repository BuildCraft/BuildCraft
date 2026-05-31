/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.marker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.Formatting;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import buildcraft.lib.tile.TileMarker;

public abstract class MarkerConnection<C extends MarkerConnection<C>> {
    public final MarkerSubCache<C> subCache;

    public MarkerConnection(MarkerSubCache<C> subCache) {
        this.subCache = subCache;
    }

    /** Removes the specified marker from this connection. This should be called via
     * {@link MarkerSubCache#removeMarker(BlockPos)}. This may need to remove itself and split itself up (if the resulting
     * connection is invalid). */
    public abstract void removeMarker(BlockPos pos);

    public abstract Collection<BlockPos> getMarkerPositions();

    @Environment(EnvType.CLIENT)
    public abstract void renderInWorld();

    public void getDebugInfo(BlockPos caller, List<String> left) {
        Collection<BlockPos> positions = getMarkerPositions();
        List<BlockPos> list = new ArrayList<>(positions);
        if (positions instanceof Set) {
            Collections.sort(list);
        }
        for (BlockPos pos : list) {
            TileMarker<C> marker = subCache.getMarker(pos);
            String s = "  " + pos + " [";
            if (marker == null) {
                s += Formatting.RED + "U";
            } else {
                s += Formatting.GREEN + "L";
            }
            if (pos.equals(caller)) {
                s += Formatting.BLACK + "S";
            } else {
                s += Formatting.AQUA + "C";
            }
            s += getTypeInfo(pos, marker);
            s += Formatting.RESET + "]";
            left.add(s);
        }
    }

    protected String getTypeInfo(BlockPos pos, @Nullable TileMarker<C> value) {
        return "";
    }
}
