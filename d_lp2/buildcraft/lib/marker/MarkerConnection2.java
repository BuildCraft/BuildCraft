package buildcraft.lib.marker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.lib.marker.MarkerCache2.PerWorld2;
import buildcraft.lib.tile.TileMarker;

public abstract class MarkerConnection2<C extends MarkerConnection2<C>> {
    public abstract void removeMarker(BlockPos pos);

    public abstract Collection<BlockPos> getMarkerPositions();

    public abstract MarkerCache2<? extends PerWorld2<C, ?>> getCache();

    public PerWorld2<C, ?> getLocalCache(World world) {
        return getCache().getSubCache(world);
    }

    @SideOnly(Side.CLIENT)
    public abstract void renderInWorld();

    @SideOnly(Side.CLIENT)
    public void getDebugInfo(World world, BlockPos caller, List<String> left) {
        left.add("");
        left.add("Connections:");
        List<BlockPos> list = new ArrayList<>(getMarkerPositions());
        Collections.sort(list);
        for (BlockPos pos : list) {
            TileMarker<C, ?> marker = getLocalCache(world).getMarker(pos);
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
    protected String getTypeInfo(BlockPos pos, TileMarker<C, ?> value) {
        return "";
    }
}
