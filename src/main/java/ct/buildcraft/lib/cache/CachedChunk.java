package ct.buildcraft.lib.cache;

import java.lang.ref.WeakReference;

import ct.buildcraft.lib.misc.ChunkUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;

public class CachedChunk implements IChunkCache {

    private final BlockEntity tile;
    private WeakReference<LevelChunk> cachedChunk;

    public CachedChunk(BlockEntity tile) {
        this.tile = tile;
    }

    @Override
    public void invalidate() {
        cachedChunk = null;
    }

    @Override
    public LevelChunk getChunk(BlockPos pos) {
        if (tile.isRemoved()) {
            cachedChunk = null;
            return null;
        }
        BlockPos tPos = tile.getBlockPos();
        if (pos.getX() >> 4 != tPos.getX() >> 4 //
            || pos.getZ() >> 4 != tPos.getZ() >> 4) {
            return null;
        }
        if (cachedChunk != null) {
            LevelChunk c = cachedChunk.get();
            if (c != null && c.getLevel().isLoaded(pos)) {
            	
                return c;
            }
            cachedChunk = null;
        }
        Level world = tile.getLevel();
        if (world == null) {
            cachedChunk = null;
            return null;
        }
        LevelChunk chunk = ChunkUtil.getChunk(world, pos, true);
        if (chunk != null && chunk.getLevel() == world) {
            cachedChunk = new WeakReference<>(chunk);
            return chunk;
        }
        return null;
    }
}
