package buildcraft.lib.compat;

import javax.annotation.Nullable;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.Chunk.EnumCreateEntityType;

import buildcraft.lib.misc.ChunkUtil;

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
            if (force || world.isBlockLoaded(pos)) {
                return world.getTileEntity(pos);
            }
            return null;
        } else {
            Chunk chunk = ChunkUtil.getChunk(world, pos, force);
            if (chunk == null) {
                return null;
            }
            return chunk.getTileEntity(pos, force ? EnumCreateEntityType.IMMEDIATE : EnumCreateEntityType.CHECK);
        }
    }

    @Override
    public IBlockState getState(World world, BlockPos pos, boolean force) {
        if (direct | force) {
            if (force || world.isBlockLoaded(pos)) {
                return world.getBlockState(pos);
            }
            return Blocks.AIR.getDefaultState();
        } else {
            Chunk chunk = ChunkUtil.getChunk(world, pos, force);
            if (chunk == null) {
                return Blocks.AIR.getDefaultState();
            }
            return chunk.getBlockState(pos);
        }
    }
}
