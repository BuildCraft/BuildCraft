/** Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.builders.schematics;

import java.util.LinkedList;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;

import buildcraft.api.blueprints.IBuilderContext;
import buildcraft.api.blueprints.SchematicBlock;

public class SchematicSeeds extends SchematicBlock {

    public Item seeds;

    public SchematicSeeds(Item seeds) {
        this.seeds = seeds;
    }

    @Override
    public void getRequirementsForPlacement(IBuilderContext context, LinkedList<ItemStack> requirements) {
        requirements.add(new ItemStack(seeds));
    }

    @Override
    public void storeRequirements(IBuilderContext context, BlockPos pos) {

    }

    @Override
    public void placeInWorld(IBuilderContext context, BlockPos pos, LinkedList<ItemStack> stacks) {
        context.world().setBlockState(pos, state, 3);
    }

    @Override
    public boolean isAlreadyBuilt(IBuilderContext context, BlockPos pos) {
        return context.world().getBlockState(pos).getBlock() == state.getBlock();
    }
}
