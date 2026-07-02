package ct.buildcraft.lib.cache;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.chunk.LevelChunk;

public interface IChunkCache {

    void invalidate();

    @Nullable
    LevelChunk getChunk(BlockPos pos);

    public enum ChunkCacheState {
        CACHED,
        NOT_CACHED,
        LOADED,
        NOT_LOADED;
    }
}
