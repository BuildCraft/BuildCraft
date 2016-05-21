package buildcraft.lib.tile;

import java.util.Collection;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Deprecated
public abstract class MarkerConnection<T extends TileMarkerBase<T, C>, C extends MarkerConnection<T, C>> {
    public final MarkerCache<T, ?>.PerWorld worldCache;

    public MarkerConnection(MarkerCache<T, ?>.PerWorld worldCache) {
        this.worldCache = worldCache;
    }

    public abstract Collection<BlockPos> getAllMarkers();

    public abstract C loadFromNBT(MarkerCache<T, C>.PerWorld worldCache, NBTTagCompound nbt);

    public abstract NBTTagCompound saveToNBT();

    /** Attempts to merge this with the other. Note that this should return null if it is impossible, and operate
     * without side effects. */
    public abstract C createMerged(C other);

    @SideOnly(Side.CLIENT)
    public abstract void renderConnections();

}
