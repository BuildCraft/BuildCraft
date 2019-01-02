package buildcraft.lib.compat;

import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import net.minecraftforge.fml.common.Loader;

public class CompatManager {
    public static final ISoftBlockAccessor blockAccessor;

    public static TileEntity getTile(World world, BlockPos pos, boolean force) {
        return blockAccessor.getTile(world, pos, force);
    }

    public static IBlockState getState(World world, BlockPos pos, boolean force) {
        return blockAccessor.getState(world, pos, force);
    }

    static {
        // Non-compile-dependent compat functions
        if (Loader.isModLoaded("cubicchunks")) {
            // Our chunk-caching optimisation is basically useless with cubic chunks -
            // we should really replace this with one in the real compat module, later.
            blockAccessor = DefaultBlockAccessor.DIRECT;
        } else {
            blockAccessor = DefaultBlockAccessor.VIA_CHUNK;
        }
    }
}
