package buildcraft.core.properties;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;

public class WorldPropertyIsFreePath extends WorldProperty {

    @Override
    public boolean get(IBlockAccess blockAccess, IBlockState state, BlockPos pos) {
        Block block = state.getBlock();
        if (block.isAir(blockAccess, pos)) return true;
        Material material = state.getBlock().getMaterial();
        return !material.blocksMovement();
    }
}
