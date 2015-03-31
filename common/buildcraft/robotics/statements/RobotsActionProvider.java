/**
 * Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.robotics.statements;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import net.minecraft.block.Block;
import net.minecraft.block.BlockWorkbench;
import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.IFluidHandler;
import buildcraft.BuildCraftRobotics;
import buildcraft.api.robots.IRequestProvider;
import buildcraft.api.statements.IActionExternal;
import buildcraft.api.statements.IActionInternal;
import buildcraft.api.statements.IActionProvider;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.transport.IPipeTile;
import buildcraft.robotics.DockingStation;
import buildcraft.robotics.RobotUtils;
import buildcraft.silicon.TileAssemblyTable;
import buildcraft.transport.PipeTransportFluids;
import buildcraft.transport.PipeTransportItems;
import buildcraft.transport.TileGenericPipe;

public class RobotsActionProvider implements IActionProvider {

	@Override
	public Collection<IActionInternal> getInternalActions(IStatementContainer container) {
		LinkedList<IActionInternal> result = new LinkedList<IActionInternal>();
		TileEntity tile = container.getTile();
		
		if (!(tile instanceof IPipeTile)) {
			return result;
		}

		ArrayList<DockingStation> stations = new ArrayList<DockingStation>();

		for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
			if (RobotUtils.getStation((IPipeTile) tile, dir) != null) {
				stations.add(RobotUtils.getStation((IPipeTile) tile, dir));
			}
		}

		if (stations.size() == 0) {
			return result;
		}

		result.add(BuildCraftRobotics.actionRobotGotoStation);
		result.add(BuildCraftRobotics.actionRobotWorkInArea);
		result.add(BuildCraftRobotics.actionRobotWakeUp);
		result.add(BuildCraftRobotics.actionRobotFilter);
		result.add(BuildCraftRobotics.actionRobotFilterTool);
		result.add(BuildCraftRobotics.actionStationForbidRobot);
		result.add(BuildCraftRobotics.actionStationForceRobot);

		if (((TileGenericPipe) tile).pipe.transport instanceof PipeTransportItems) {
			result.add(BuildCraftRobotics.actionStationRequestItems);
			result.add(BuildCraftRobotics.actionStationAcceptItems);
		}

		if (((TileGenericPipe) tile).pipe.transport instanceof PipeTransportFluids) {
			result.add(BuildCraftRobotics.actionStationAcceptFluids);
		}

		for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
			TileEntity sideTile = ((TileGenericPipe) tile).getTile(dir);
			Block sideBlock = ((TileGenericPipe) tile).getBlock(dir);

			if (sideTile instanceof IPipeTile) {
				continue;
			}
			
			if (sideTile instanceof IInventory) {
				result.add(BuildCraftRobotics.actionStationProvideItems);
			}

			if (sideTile instanceof IFluidHandler) {
				result.add(BuildCraftRobotics.actionStationProvideFluids);
			}

			if (sideTile instanceof IRequestProvider) {
				result.add(BuildCraftRobotics.actionStationMachineRequestItems);
			}

			if (sideTile instanceof TileEntityFurnace
					|| sideTile instanceof TileAssemblyTable
					|| sideBlock instanceof BlockWorkbench) {
				result.add(BuildCraftRobotics.actionRobotAllowCraft);
			}
		}

		return result;
	}

	@Override
	public Collection<IActionExternal> getExternalActions(ForgeDirection side, TileEntity tile) {
		return null;
	}

}
