package buildcraft.lib.marker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.lib.marker.MarkerCache2.SubCache2;
import buildcraft.lib.tile.TileMarker;

public abstract class MarkerConnection2<C extends MarkerConnection2<C>> {
    public final SubCache2<C> subCache;

    public MarkerConnection2(SubCache2<C> subCache) {
        this.subCache = subCache;
    }

    /** Removes the specified marker from this connection. This should be called via
     * {@link SubCache2#removeMarker(BlockPos)}. This may need to remove itself and split itself up (if the resulting
     * connection is invalid). */
    public abstract void removeMarker(BlockPos pos);

    public abstract Collection<BlockPos> getMarkerPositions();

    @SideOnly(Side.CLIENT)
    public abstract void renderInWorld();

    @SideOnly(Side.CLIENT)
    public void getDebugInfo(BlockPos caller, List<String> left) {
        left.add("");
        left.add("Connections:");
        List<BlockPos> list = new ArrayList<>(getMarkerPositions());
        Collections.sort(list);
        for (BlockPos pos : list) {
            TileMarker<C> marker = subCache.getMarker(pos);
            String s = " - " + pos + " [";
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
    protected String getTypeInfo(BlockPos pos, TileMarker<C> value) {
        return "";
    }
}
