// STUB(R.Chen): 1.12 IChunkProvider — removed in 1.20.
package net.minecraft.world.chunk;

public interface IChunkProvider {
    WorldChunk provideChunk(int x, int z);
    WorldChunk getLoadedChunk(int x, int z);
    boolean isChunkGeneratedAt(int x, int z);
}
