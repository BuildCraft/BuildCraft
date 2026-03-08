package buildcraft.lib.cache;

import buildcraft.lib.misc.ChunkUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import java.lang.ref.WeakReference;

public class CachedChunk implements IChunkCache {

    private final TileEntity tile;
    private WeakReference<Chunk> cachedChunk;

    public CachedChunk(TileEntity tile) {
        this.tile = tile;
    }

    @Override
    public void invalidate() {
        cachedChunk = null;
    }

    @Override
    public Chunk getChunk(BlockPos pos) {
        if (tile.isRemoved()) {
            cachedChunk = null;
            return null;
        }
        BlockPos tPos = tile.getBlockPos();
        if (pos.getX() >> 4 != tPos.getX() >> 4 //
                || pos.getZ() >> 4 != tPos.getZ() >> 4)
        {
            return null;
        }
        if (cachedChunk != null) {
            Chunk c = cachedChunk.get();
//            if (c != null && c.isLoaded())
            if (c != null && c.loaded) {
                return c;
            }
            cachedChunk = null;
        }
        World world = tile.getLevel();
        if (world == null) {
            cachedChunk = null;
            return null;
        }
        Chunk chunk = ChunkUtil.getChunk(world, pos, true);
        if (chunk != null && chunk.getLevel() == world) {
            cachedChunk = new WeakReference<>(chunk);
            return chunk;
        }
        return null;
    }
}
