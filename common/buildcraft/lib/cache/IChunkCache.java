package buildcraft.lib.cache;

import javax.annotation.Nullable;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;

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
