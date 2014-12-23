/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.energy;

import java.util.LinkedList;

import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;

import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

import buildcraft.api.blueprints.IBuilderContext;
import buildcraft.api.blueprints.SchematicTile;

public class SchematicEngine extends SchematicTile {

	@Override
	public void rotateLeft(IBuilderContext context) {
		int o = tileNBT.getInteger("orientation");

		o = EnumFacing.values()[o].rotateY().ordinal();

		tileNBT.setInteger("orientation", o);
	}

	@Override
	public void initializeFromObjectAt(IBuilderContext context, BlockPos pos) {
		super.initializeFromObjectAt(context, pos);

		TileEngine engine = (TileEngine) context.world().getTileEntity(pos);

		tileNBT.setInteger("orientation", engine.orientation.ordinal());
		tileNBT.removeTag("progress");
		tileNBT.removeTag("energy");
		tileNBT.removeTag("heat");
		tileNBT.removeTag("tankFuel");
		tileNBT.removeTag("tankCoolant");
	}

	@Override
	public void placeInWorld(IBuilderContext context, BlockPos pos, LinkedList<ItemStack> stacks) {
		super.placeInWorld(context, pos, stacks);

		TileEngine engine = (TileEngine) context.world().getTileEntity(pos);

		engine.orientation = EnumFacing.getFront(tileNBT.getInteger("orientation"));
		engine.sendNetworkUpdate();
	}

	@Override
	public void postProcessing (IBuilderContext context, BlockPos pos) {
		TileEngine engine = (TileEngine) context.world().getTileEntity(pos);

		if (engine != null) {
			engine.orientation = EnumFacing.getFront(tileNBT.getInteger("orientation"));
			engine.sendNetworkUpdate();
			context.world().markBlockForUpdate(pos);
			context.world().notifyNeighborsOfStateChange(pos, state.getBlock());
		}
	}

	@Override
	public BuildingStage getBuildStage() {
		return BuildingStage.STANDALONE;
	}

}
