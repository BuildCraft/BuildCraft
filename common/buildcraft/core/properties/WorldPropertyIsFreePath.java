package buildcraft.core.properties;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import buildcraft.api.core.BuildCraftAPI;

public class WorldPropertyIsFreePath extends WorldProperty {

    @Override
    public boolean get(IBlockAccess blockAccess, IBlockState state, BlockPos pos) {
        Block block = state.getBlock();
        if (block.isAir(blockAccess, pos)) return true;
        if (BuildCraftAPI.softBlocks.contains(block)) return true;
        if (blockAccess instanceof World) {
            World world = (World) blockAccess;
            if (hasNoCollisionBoundingBox(world, state, pos)) return true;
        }
        return false;
    }

    private static boolean hasNoCollisionBoundingBox(World world, IBlockState state, BlockPos pos) {
        return state.getBlock().getCollisionBoundingBox(world, pos, state) == null;
    }
}
