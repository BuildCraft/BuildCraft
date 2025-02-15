package buildcraft.lib.cache;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.chunk.LevelChunk;

import javax.annotation.Nullable;

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
