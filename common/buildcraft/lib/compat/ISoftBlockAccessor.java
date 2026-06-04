package buildcraft.lib.compat;

import javax.annotation.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface ISoftBlockAccessor {
    /** @param force If true then the chunk containing the tile will be loaded from disk, false if this should only get
     *            the tile entity if it is currently loaded */
    @Nullable
    BlockEntity getTile(World world, BlockPos pos, boolean force);

    /** @param force If true then the chunk containing the tile will be loaded from disk, false if this should only get
     *            the tile entity if it is currently loaded */
    BlockState getState(World world, BlockPos pos, boolean force);
}
