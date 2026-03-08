package buildcraft.lib.compat;

import buildcraft.lib.misc.ChunkUtil;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

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
    public TileEntity getTile(World world, BlockPos pos, boolean force) {
        if (direct | force) {
            if (force || world.isLoaded(pos)) {
                return world.getBlockEntity(pos);
            }
            return null;
        } else {
            Chunk chunk = ChunkUtil.getChunk(world, pos, force);
            if (chunk == null) {
                return null;
            }
            return chunk.getBlockEntity(pos, force ? Chunk.CreateEntityType.IMMEDIATE : Chunk.CreateEntityType.CHECK);
        }
    }

    @Override
    public BlockState getState(World world, BlockPos pos, boolean force) {
        if (direct | force) {
            if (force || world.isLoaded(pos)) {
                return world.getBlockState(pos);
            }
            return Blocks.AIR.defaultBlockState();
        } else {
            Chunk chunk = ChunkUtil.getChunk(world, pos, force);
            if (chunk == null) {
                return Blocks.AIR.defaultBlockState();
            }
            return chunk.getBlockState(pos);
        }
    }
}
