package buildcraft.core.lib.utils;

import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.ChunkProviderServer;

public final class ThreadSafeUtils {
    private static final ThreadLocal<Chunk> lastChunk = new ThreadLocal<Chunk>();

    private ThreadSafeUtils() {

    }

    public static Chunk getChunk(World world, int x, int z) {
        Chunk chunk;
        chunk = lastChunk.get();

        if (chunk != null) {
            if (chunk.isLoaded()) {
                if (chunk.getWorld() == world && chunk.xPosition == x && chunk.zPosition == z) {
                    return chunk;
                }
            } else {
                lastChunk.set(null);
            }
        }

        IChunkProvider provider = world.getChunkProvider();
        // These probably won't guarantee full thread safety, but it's our best bet.
        if (!Utils.CAULDRON_DETECTED && provider instanceof ChunkProviderServer) {
            // Slight optimization
            chunk = (Chunk) ((ChunkProviderServer) provider).id2ChunkMap.getValueByKey(ChunkCoordIntPair.chunkXZ2Int(x, z));
        } else {
            chunk = provider.chunkExists(x, z) ? provider.provideChunk(x, z) : null;
        }

        if (chunk != null) {
            lastChunk.set(chunk);
        }
        return chunk;
    }
}
