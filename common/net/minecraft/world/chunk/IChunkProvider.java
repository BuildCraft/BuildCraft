// STUB(R.Chen): 1.12 IChunkProvider — removed in 1.20.
package net.minecraft.world.chunk;

public interface IChunkProvider {
    net.minecraft.world.chunk.Chunk provideChunk(int x, int z);
    net.minecraft.world.chunk.Chunk getLoadedChunk(int x, int z);
    boolean isChunkGeneratedAt(int x, int z);
}
