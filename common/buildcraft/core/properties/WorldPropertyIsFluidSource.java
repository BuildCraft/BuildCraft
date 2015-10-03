/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.properties;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidBlock;

public class WorldPropertyIsFluidSource extends WorldProperty {

    @Override
    public boolean get(IBlockAccess blockAccess, IBlockState state, BlockPos pos) {
        Block block = state.getBlock();
        if (block instanceof BlockLiquid) {
            return (Integer) state.getValue(BlockLiquid.LEVEL) == 0;
        } else if (block instanceof IFluidBlock) {
            FluidStack fluid = ((IFluidBlock) block).drain((World) blockAccess, pos, false);
            return fluid != null && fluid.amount > 0;

        } else {
            return false;
        }
    }
}
