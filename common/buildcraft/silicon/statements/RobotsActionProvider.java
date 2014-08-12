/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.silicon.statements;

import java.util.ArrayList;
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
import buildcraft.core.robots.DockingStation;
import buildcraft.core.robots.boards.BoardRobotCrafter;
import buildcraft.transport.PipeTransportItems;
import buildcraft.transport.TileGenericPipe;

public class RobotsActionProvider implements IActionProvider {

	@Override
	public Collection<IAction> getPipeActions(IPipeTile pipe) {
		LinkedList<IAction> result = new LinkedList<IAction>();

		ArrayList<DockingStation> stations = new ArrayList<DockingStation>();

		for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
			if (((TileGenericPipe) pipe).getStation(dir) != null) {
				stations.add(((TileGenericPipe) pipe).getStation(dir));
			}
		}

		if (stations.size() == 0) {
			return result;
		}

		result.add(BuildCraftSilicon.actionRobotGotoStation);
		result.add(BuildCraftSilicon.actionRobotWorkInArea);
		result.add(BuildCraftSilicon.actionRobotWakeUp);
		result.add(BuildCraftSilicon.actionRobotFilter);
		result.add(BuildCraftSilicon.actionStationForbidRobot);

		for (DockingStation s : stations) {
			if (s.robotTaking() != null && s.robotTaking().getBoard() instanceof BoardRobotCrafter) {
				result.add(BuildCraftSilicon.actionRobotCraft);
			}
		}

		for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
			if (((TileGenericPipe) pipe).getTile(dir) instanceof IInventory) {
				result.add(BuildCraftSilicon.actionStationProvideItems);
				result.add(BuildCraftSilicon.actionStationRequestItems);
			}

			if (((TileGenericPipe) pipe).pipe.transport instanceof PipeTransportItems) {
				result.add(BuildCraftSilicon.actionStationDropInPipe);
			}
		}

		return result;
	}

	@Override
	public Collection<IAction> getNeighborActions(Block block, TileEntity tile) {
		return null;
	}

}
