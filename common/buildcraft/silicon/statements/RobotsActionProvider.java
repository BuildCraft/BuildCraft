/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.silicon.statements;

import java.util.Collection;
import java.util.LinkedList;

import net.minecraft.block.Block;
import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.TileEntity;

import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.BuildCraftSilicon;
import buildcraft.api.gates.IAction;
import buildcraft.api.gates.IActionProvider;
import buildcraft.api.transport.IPipeTile;
import buildcraft.transport.TileGenericPipe;

public class RobotsActionProvider implements IActionProvider {

	@Override
	public Collection<IAction> getPipeActions(IPipeTile pipe) {
		LinkedList<IAction> result = new LinkedList<IAction>();

		boolean stationFound = false;

		for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
			if (((TileGenericPipe) pipe).getStation(dir) != null) {
				stationFound = true;
				break;
			}
		}

		if (!stationFound) {
			return result;
		}

		result.add(BuildCraftSilicon.actionRobotGotoStation);
		result.add(BuildCraftSilicon.actionRobotWorkInArea);
		result.add(BuildCraftSilicon.actionRobotWakeUp);
		result.add(BuildCraftSilicon.actionStationForbidRobot);

		for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
			if (((TileGenericPipe) pipe).getTile(dir) instanceof IInventory) {
				result.add(BuildCraftSilicon.actionStationProvideItems);
				result.add(BuildCraftSilicon.actionStationRequestItems);
			}
		}

		return result;
	}

	@Override
	public Collection<IAction> getNeighborActions(Block block, TileEntity tile) {
		return null;
	}

}
