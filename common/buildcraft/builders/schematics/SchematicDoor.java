/** Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.builders.schematics;

import java.util.LinkedList;

import net.minecraft.block.BlockDoor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;

import buildcraft.api.blueprints.IBuilderContext;
import buildcraft.api.blueprints.SchematicBlock;

public class SchematicDoor extends SchematicBlock {

    final ItemStack stack;

    public SchematicDoor(ItemStack stack) {
        this.stack = stack;
    }

    @Override
    public void getRequirementsForPlacement(IBuilderContext context, LinkedList<ItemStack> requirements) {
        if (state.getValue(BlockDoor.HALF) == BlockDoor.EnumDoorHalf.LOWER) {
            requirements.add(stack.copy());
        }
    }

    @Override
    public void storeRequirements(IBuilderContext context, BlockPos pos) {
        // cancel requirements reading
    }

    @Override
    public boolean doNotBuild() {
        return state.getValue(BlockDoor.HALF) == BlockDoor.EnumDoorHalf.UPPER;
    }

    @Override
    public boolean isAlreadyBuilt(IBuilderContext context, BlockPos pos) {
        return state.getBlock() == context.world().getBlockState(pos).getBlock();
    }

    @Override
    public void placeInWorld(IBuilderContext context, BlockPos pos, LinkedList<ItemStack> stacks) {
        context.world().setBlockState(pos, state);
        context.world().setBlockState(pos.up(), state.withProperty(BlockDoor.HALF, BlockDoor.EnumDoorHalf.UPPER));
    }
}
