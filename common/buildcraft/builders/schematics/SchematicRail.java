/** Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.builders.schematics;

import java.util.LinkedList;

import net.minecraft.block.BlockRail;
import net.minecraft.block.BlockRailBase;
import net.minecraft.block.BlockRailBase.EnumRailDirection;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;

import buildcraft.api.blueprints.IBuilderContext;
import buildcraft.api.blueprints.SchematicBlock;
import buildcraft.api.properties.BuildCraftProperties;

public class SchematicRail extends SchematicBlock {
    private static final IBlockState defaultDirection = Blocks.rail.getDefaultState().withProperty(BlockRail.SHAPE, EnumRailDirection.NORTH_SOUTH);

    @Override
    public void rotateLeft(IBuilderContext context) {
        EnumRailDirection direction = (EnumRailDirection) state.getValue(BlockRail.SHAPE);
        state = state.withProperty(BlockRail.SHAPE, getRotatedDirection(direction));
    }

    @Override
    public void placeInWorld(IBuilderContext context, BlockPos pos, LinkedList<ItemStack> stacks) {
        // If we set it to NORTH_SOUTH here (and don't update) then when we set it to the actual state in the post
        // processing it just ignores the update because they are the same
        if (state == defaultDirection)
            context.world().setBlockState(pos, defaultDirection, BuildCraftProperties.MARK_BLOCK_FOR_UPDATE);
        else
            context.world().setBlockState(pos, defaultDirection, BuildCraftProperties.UPDATE_NONE);
    }

    @Override
    public boolean isAlreadyBuilt(IBuilderContext context, BlockPos pos) {
        return state.getBlock() == context.world().getBlockState(pos).getBlock();
    }

    @Override
    public void postProcessing(IBuilderContext context, BlockPos pos) {
        context.world().setBlockState(pos, state);
    }

    public void setMetaData(BlockRailBase.EnumRailDirection newValue) {
        state = state.withProperty(BlockRail.SHAPE, newValue);
    }

    private EnumRailDirection getRotatedDirection(EnumRailDirection old) {
        switch (old) {
            case ASCENDING_EAST:
                return EnumRailDirection.ASCENDING_SOUTH;
            case ASCENDING_NORTH:
                return EnumRailDirection.ASCENDING_EAST;
            case ASCENDING_SOUTH:
                return EnumRailDirection.ASCENDING_WEST;
            case ASCENDING_WEST:
                return EnumRailDirection.ASCENDING_NORTH;
            case EAST_WEST:
                return EnumRailDirection.NORTH_SOUTH;
            case NORTH_EAST:
                return EnumRailDirection.SOUTH_EAST;
            case NORTH_SOUTH:
                return EnumRailDirection.EAST_WEST;
            case NORTH_WEST:
                return EnumRailDirection.NORTH_EAST;
            case SOUTH_EAST:
                return EnumRailDirection.SOUTH_WEST;
            case SOUTH_WEST:
                return EnumRailDirection.NORTH_WEST;
            default:
                return EnumRailDirection.NORTH_SOUTH;
        }
    }
}
