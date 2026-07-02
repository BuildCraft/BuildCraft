package ct.buildcraft.lib.cache;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public interface ITileCache {

    /** Call this in {@link EntityBlock#invalidate()} to remove everything that has been cached. */
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
