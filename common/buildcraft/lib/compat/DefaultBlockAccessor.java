package buildcraft.lib.compat;

import buildcraft.lib.misc.ChunkUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunk.EntityCreationType;

import javax.annotation.Nullable;

public enum DefaultBlockAccessor implements ISoftBlockAccessor {
    DIRECT(true),
    VIA_CHUNK(false);

    private final boolean direct;

    DefaultBlockAccessor(boolean direct) {
        this.direct = direct;
    }

    @Override
    @Nullable
    public BlockEntity getTile(Level world, BlockPos pos, boolean force) {
        if (direct | force) {
            if (force || world.isLoaded(pos)) {
                return world.getBlockEntity(pos);
            }
            return null;
        } else {
            LevelChunk chunk = ChunkUtil.getChunk(world, pos, force);
            if (chunk == null) {
                return null;
            }
            return chunk.getBlockEntity(pos, force ? EntityCreationType.IMMEDIATE : EntityCreationType.CHECK);
        }
    }

    @Override
    public BlockState getState(Level world, BlockPos pos, boolean force) {
        if (direct | force) {
            if (force || world.isLoaded(pos)) {
                return world.getBlockState(pos);
            }
            return Blocks.AIR.defaultBlockState();
        } else {
            LevelChunk chunk = ChunkUtil.getChunk(world, pos, force);
            if (chunk == null) {
                return Blocks.AIR.defaultBlockState();
            }
            return chunk.getBlockState(pos);
        }
    }
}
