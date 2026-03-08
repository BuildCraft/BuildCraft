package buildcraft.lib.cache;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;

import javax.annotation.Nullable;

public interface IChunkCache {

    void invalidate();

    @Nullable
    Chunk getChunk(BlockPos pos);

    public enum ChunkCacheState {
        CACHED,
        NOT_CACHED,
        LOADED,
        NOT_LOADED;
    }
}
