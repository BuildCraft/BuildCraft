/** Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.builders.schematics;

import java.util.LinkedList;

import net.minecraft.block.BlockBed;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

import buildcraft.api.blueprints.IBuilderContext;
import buildcraft.api.blueprints.SchematicBlock;

public class SchematicBed extends SchematicBlock {

    @Override
    public void getRequirementsForPlacement(IBuilderContext context, LinkedList<ItemStack> requirements) {
        if (state.getValue(BlockBed.PART) == BlockBed.EnumPartType.HEAD) {
            requirements.add(new ItemStack(Items.bed));
        }
    }

    @Override
    public void storeRequirements(IBuilderContext context, BlockPos pos) {
        // cancel requirements reading
    }

    @Override
    public void rotateLeft(IBuilderContext context) {
        IBlockState oldState = state;
        EnumFacing oldFacing = (EnumFacing) oldState.getValue(BlockBed.FACING);
        state = oldState.withProperty(BlockBed.FACING, oldFacing.rotateY());
    }

    @Override
    public void placeInWorld(IBuilderContext context, BlockPos pos, LinkedList<ItemStack> stacks) {
        context.world().setBlockState(pos, state.withProperty(BlockBed.PART, BlockBed.EnumPartType.HEAD));
        BlockPos feetPos = pos.offset(getFace(BlockBed.FACING), -1);
        context.world().setBlockState(feetPos, state.withProperty(BlockBed.PART, BlockBed.EnumPartType.FOOT));
    }

    @Override
    public boolean doNotBuild() {
        return state.getValue(BlockBed.PART) == BlockBed.EnumPartType.FOOT;
    }
}
