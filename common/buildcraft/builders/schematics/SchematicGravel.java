/**
 * Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.builders.schematics;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;

import buildcraft.api.blueprints.IBuilderContext;
import buildcraft.api.blueprints.SchematicBlock;

public class SchematicGravel extends SchematicBlock {

    @Override
    public void getRequirementsForPlacement(IBuilderContext context, List<ItemStack> requirements) {
        requirements.add(new ItemStack(Blocks.gravel));
    }

    @Override
    public void storeRequirements(IBuilderContext context, BlockPos pos) {
        // cancel requirements reading
    }

    @Override
    public void placeInWorld(IBuilderContext context, BlockPos pos, List<ItemStack> stacks) {
        context.world().setBlockState(pos, Blocks.gravel.getDefaultState());
    }

    @Override
    public boolean isAlreadyBuilt(IBuilderContext context, BlockPos pos) {
        Block block = context.world().getBlockState(pos).getBlock();

        return block == Blocks.gravel;
    }
}
