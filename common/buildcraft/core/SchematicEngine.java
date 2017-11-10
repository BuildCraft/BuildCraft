/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core;

import java.util.LinkedList;

import net.minecraft.item.ItemStack;

import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.api.blueprints.IBuilderContext;
import buildcraft.api.blueprints.SchematicTile;
import buildcraft.core.lib.engines.TileEngineBase;

public class SchematicEngine extends SchematicTile {

	@Override
	public void rotateLeft(IBuilderContext context) {
		int o = tileNBT.getInteger("orientation");

		o = ForgeDirection.values()[o].getRotation(ForgeDirection.UP).ordinal();

		tileNBT.setInteger("orientation", o);
	}

	@Override
	public void initializeFromObjectAt(IBuilderContext context, int x, int y, int z) {
		super.initializeFromObjectAt(context, x, y, z);

		TileEngineBase engine = (TileEngineBase) context.world().getTileEntity(x, y, z);

		tileNBT.setInteger("orientation", engine.orientation.ordinal());
		tileNBT.removeTag("progress");
		tileNBT.removeTag("energy");
		tileNBT.removeTag("heat");
		tileNBT.removeTag("tankFuel");
		tileNBT.removeTag("tankCoolant");
	}

	@Override
	public void placeInWorld(IBuilderContext context, int x, int y, int z, LinkedList<ItemStack> stacks) {
		super.placeInWorld(context, x, y, z, stacks);

		TileEngineBase engine = (TileEngineBase) context.world().getTileEntity(x, y, z);

		engine.orientation = ForgeDirection.getOrientation(tileNBT.getInteger("orientation"));
		engine.sendNetworkUpdate();
	}

	@Override
	public void postProcessing(IBuilderContext context, int x, int y, int z) {
		TileEngineBase engine = (TileEngineBase) context.world().getTileEntity(x, y, z);

		if (engine != null) {
			engine.orientation = ForgeDirection.getOrientation(tileNBT.getInteger("orientation"));
			engine.sendNetworkUpdate();
			context.world().markBlockForUpdate(x, y, z);
			context.world().notifyBlocksOfNeighborChange(x, y, z, block);
		}
	}

	@Override
	public BuildingStage getBuildStage() {
		return BuildingStage.STANDALONE;
	}

}
