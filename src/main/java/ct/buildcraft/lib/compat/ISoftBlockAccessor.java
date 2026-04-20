package ct.buildcraft.lib.compat;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public interface ISoftBlockAccessor {
    /** @param force If true then the chunk containing the tile will be loaded from disk, false if this should only get
     *            the tile entity if it is currently loaded */
    @Nullable
    BlockEntity getTile(Level world, BlockPos pos, boolean force);

    /** @param force If true then the chunk containing the tile will be loaded from disk, false if this should only get
     *            the tile entity if it is currently loaded */
    BlockState getState(Level world, BlockPos pos, boolean force);
}
