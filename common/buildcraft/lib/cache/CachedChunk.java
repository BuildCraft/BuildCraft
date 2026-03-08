package buildcraft.lib.cache;

import buildcraft.lib.misc.ChunkUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;

import java.lang.ref.WeakReference;

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
                || pos.getZ() >> 4 != tPos.getZ() >> 4)
        {
            return null;
        }
        if (cachedChunk != null) {
            LevelChunk c = cachedChunk.get();
//            if (c != null && c.isLoaded())
            if (c != null && c.isInLevel()) {
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
