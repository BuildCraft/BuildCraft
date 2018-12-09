package buildcraft.lib.cache;

import javax.annotation.Nullable;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

public interface ITileCache {

    /** Call this in {@link TileEntity#invalidate()} to remove everything that has been cached. */
    void invalidate();

    @Nullable
    TileCacheRet getTile(BlockPos pos);

    @Nullable
    TileCacheRet getTile(EnumFacing offset);

    public enum TileCacheState {
        CACHED,
        NOT_CACHED,
        NOT_PRESENT;
    }
}
