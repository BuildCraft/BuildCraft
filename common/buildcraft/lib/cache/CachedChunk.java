package buildcraft.lib.cache;

import java.lang.ref.WeakReference;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;

import buildcraft.lib.misc.ChunkUtil;

public class CachedChunk implements IChunkCache {

    private final BlockEntity tile;
    private WeakReference<WorldChunk> cachedChunk;

    public CachedChunk(BlockEntity tile) {
        this.tile = tile;
    }

    @Override
    public void invalidate() {
        cachedChunk = null;
    }

    @Override
    public WorldChunk getChunk(BlockPos pos) {
        if (tile.isRemoved()) {
            cachedChunk = null;
            return null;
        }
        BlockPos tPos = tile.getPos();
        if (pos.getX() >> 4 != tPos.getX() >> 4 //
            || pos.getZ() >> 4 != tPos.getZ() >> 4) {
            return null;
        }
        World world = tile.getWorld();
        if (world == null) {
            cachedChunk = null;
            return null;
        }
        if (cachedChunk != null) {
            // STUB(R.Chen): Forge Chunk.isLoaded() removed; validity is now keyed on the cached chunk's
            // world still matching the tile's world (Yarn WorldChunk has no isLoaded predicate).
            WorldChunk c = cachedChunk.get();
            if (c != null && c.world == world) {
                return c;
            }
            cachedChunk = null;
        }
        WorldChunk chunk = ChunkUtil.getChunk(world, pos, true);
        if (chunk != null && chunk.world == world) {
            cachedChunk = new WeakReference<>(chunk);
            return chunk;
        }
        return null;
    }
}
