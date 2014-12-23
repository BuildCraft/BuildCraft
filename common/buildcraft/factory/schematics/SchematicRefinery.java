/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.factory.schematics;

import java.util.LinkedList;

import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import buildcraft.BuildCraftFactory;
import buildcraft.api.blueprints.IBuilderContext;
import buildcraft.api.blueprints.SchematicTile;
import buildcraft.core.BlockBuildCraft;

public class SchematicRefinery extends SchematicTile {

	@Override
	public void getRequirementsForPlacement(IBuilderContext context, LinkedList<ItemStack> requirements) {
		requirements.add(new ItemStack(BuildCraftFactory.refineryBlock));
	}

	@Override
	public void storeRequirements(IBuilderContext context, BlockPos pos) {

	}

	@Override
	public void rotateLeft(IBuilderContext context) {
		state = state.withProperty(BlockBuildCraft.FACING_PROP, ((EnumFacing) state.getValue(BlockBuildCraft.FACING_PROP)).rotateY());
	}

	@Override
	public void initializeFromObjectAt(IBuilderContext context, BlockPos pos) {
		super.initializeFromObjectAt(context, pos);

		tileNBT.removeTag("tank1");
		tileNBT.removeTag("tank2");
		tileNBT.removeTag("result");
		tileNBT.removeTag("mjStored");
	}

	@Override
	public void placeInWorld(IBuilderContext context, BlockPos pos, LinkedList<ItemStack> stacks) {
		// to support refineries coming from older blueprints
		tileNBT.removeTag("tank1");
		tileNBT.removeTag("tank2");
		tileNBT.removeTag("result");
		tileNBT.removeTag("mjStored");

		super.placeInWorld(context, pos, stacks);
	}

}
