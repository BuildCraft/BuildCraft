/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.builders.schematics;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;

import buildcraft.api.blueprints.IBuilderContext;
import buildcraft.api.blueprints.SchematicBlock;

public class SchematicFree extends SchematicBlock {
    @Override
    public void getRequirementsForPlacement(IBuilderContext context, List<ItemStack> requirements) {

    }

    @Override
    public void storeRequirements(IBuilderContext context, BlockPos pos) {
        // cancel requirements reading
    }

    @Override
    public List<ItemStack> getStacksToDisplay(List<ItemStack> stackConsumed) {
        List<ItemStack> displayStacks = new ArrayList<ItemStack>();
        displayStacks.add(new ItemStack(state.getBlock(), 1, state.getBlock().getMetaFromState(state)));
        return displayStacks;
    }
}
