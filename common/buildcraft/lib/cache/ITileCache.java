package buildcraft.lib.cache;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;

public interface ITileCache {

    /** Call this in {@link TileEntity#setRemoved()} to remove everything that has been cached. */
    void invalidate();

    @Nullable
    TileCacheRet getTile(BlockPos pos);

    @Nullable
    TileCacheRet getTile(Direction offset);

    public enum TileCacheState {
        CACHED,
        NOT_CACHED,
        NOT_PRESENT;
    }
}
