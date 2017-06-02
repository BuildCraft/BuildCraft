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
import net.minecraft.util.text.TextFormatting;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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

    @SideOnly(Side.CLIENT)
    public abstract void renderInWorld();

    @SideOnly(Side.CLIENT)
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
                s += TextFormatting.RED + "U";
            } else {
                s += TextFormatting.GREEN + "L";
            }
            if (pos.equals(caller)) {
                s += TextFormatting.BLACK + "S";
            } else {
                s += TextFormatting.AQUA + "C";
            }
            s += getTypeInfo(pos, marker);
            s += TextFormatting.RESET + "]";
            left.add(s);
        }
    }

    @SideOnly(Side.CLIENT)
    protected String getTypeInfo(BlockPos pos, @Nullable TileMarker<C> value) {
        return "";
    }
}
