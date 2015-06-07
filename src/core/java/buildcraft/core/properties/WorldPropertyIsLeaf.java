/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.properties;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.oredict.OreDictionary;

public class WorldPropertyIsLeaf extends WorldProperty {

    private int leavesId = 0;

    public WorldPropertyIsLeaf() {
        leavesId = OreDictionary.getOreID("treeLeaves");
    }

    @Override
    public boolean get(IBlockAccess blockAccess, IBlockState state, BlockPos pos) {
        Block block = state.getBlock();
        if (block == null) {
            return false;
        } else {
            ItemStack stack = new ItemStack(block, 1, 0);

            if (stack.getItem() != null) {
                for (int id : OreDictionary.getOreIDs(stack)) {
                    if (id == leavesId) {
                        return true;
                    }
                }
            }
        }

        return false;
    }
}
