/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.builders.schematics;

import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

import buildcraft.api.blueprints.IBuilderContext;
import buildcraft.core.builders.schematics.SchematicBlockFloored;

public class SchematicRedstoneWire extends SchematicBlockFloored {
    final ItemStack customStack;

    public SchematicRedstoneWire(ItemStack customStack) {
        this.customStack = customStack;
    }

    @Override
    public void getRequirementsForPlacement(IBuilderContext context, List<ItemStack> requirements) {
        requirements.add(customStack.copy());
    }

    @Override
    public void storeRequirements(IBuilderContext context, BlockPos pos) {

    }

    @Override
    public void placeInWorld(IBuilderContext context, BlockPos pos, List<ItemStack> stacks) {
        context.world().setBlockState(pos, state, 3);
    }

    @Override
    public boolean isAlreadyBuilt(IBuilderContext context, BlockPos pos) {
        return state.getBlock() == context.world().getBlockState(pos).getBlock();
    }
}
