package buildcraft.lib.compat;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fml.ModList;

public class CompatManager {
    public static final ISoftBlockAccessor blockAccessor;

    public static BlockEntity getTile(Level world, BlockPos pos, boolean force) {
        return blockAccessor.getTile(world, pos, force);
    }

    public static BlockState getState(Level world, BlockPos pos, boolean force) {
        return blockAccessor.getState(world, pos, force);
    }

    static {
        // Non-compile-dependent compat functions
        if (ModList.get().isLoaded("cubicchunks")) {
            // Our chunk-caching optimisation is basically useless with cubic chunks -
            // we should really replace this with one in the real compat module, later.
            blockAccessor = DefaultBlockAccessor.DIRECT;
        } else {
            blockAccessor = DefaultBlockAccessor.VIA_CHUNK;
        }
    }
}
