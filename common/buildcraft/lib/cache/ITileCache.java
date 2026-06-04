package buildcraft.lib.cache;

import javax.annotation.Nullable;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public interface ITileCache {

    /** Call this in {@link BlockEntity#markRemoved()} to remove everything that has been cached. */
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
