/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.builders.triggers;

import java.util.LinkedList;

import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;

import buildcraft.api.filler.FillerManager;
import buildcraft.api.gates.IAction;
import buildcraft.api.gates.IActionProvider;
import buildcraft.builders.TileFiller;

public class BuildersActionProvider implements IActionProvider {

	@Override
	public LinkedList<IAction> getNeighborActions(Block block, TileEntity tile) {
		LinkedList<IAction> actions = new LinkedList<IAction>();
		if (tile instanceof TileFiller) {
			actions.addAll(FillerManager.registry.getActions());
		}
		return actions;
	}
}
