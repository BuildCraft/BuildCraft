/**
 * Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.robotics.statements;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.BlockWorkbench;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraftforge.common.util.ForgeDirection;
import buildcraft.BuildCraftRobotics;
import buildcraft.api.robots.DockingStation;
import buildcraft.api.statements.IActionExternal;
import buildcraft.api.statements.IActionInternal;
import buildcraft.api.statements.IActionProvider;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.transport.IPipeTile;
import buildcraft.api.transport.IPipeTile.PipeType;
import buildcraft.robotics.RobotUtils;
import buildcraft.silicon.TileAssemblyTable;
import buildcraft.transport.TileGenericPipe;

public class RobotsActionProvider implements IActionProvider {

	@Override
	public Collection<IActionInternal> getInternalActions(IStatementContainer container) {
		LinkedList<IActionInternal> result = new LinkedList<IActionInternal>();
		TileEntity tile = container.getTile();

		if (!(tile instanceof IPipeTile)) {
			return result;
		}

		IPipeTile pipeTile = (IPipeTile) tile;

		List<DockingStation> stations = RobotUtils.getStations(pipeTile);

		if (stations.size() == 0) {
			return result;
		}

		result.add(BuildCraftRobotics.actionRobotGotoStation);
		result.add(BuildCraftRobotics.actionRobotWorkInArea);
		result.add(BuildCraftRobotics.actionRobotLoadUnloadArea);
		result.add(BuildCraftRobotics.actionRobotWakeUp);
		result.add(BuildCraftRobotics.actionRobotFilter);
		result.add(BuildCraftRobotics.actionRobotFilterTool);
		result.add(BuildCraftRobotics.actionStationForbidRobot);
		result.add(BuildCraftRobotics.actionStationForceRobot);

		if (pipeTile.getPipeType() == PipeType.ITEM) {
			result.add(BuildCraftRobotics.actionStationRequestItems);
			result.add(BuildCraftRobotics.actionStationAcceptItems);
		}

		if (pipeTile.getPipeType() == PipeType.FLUID) {
			result.add(BuildCraftRobotics.actionStationAcceptFluids);
		}

		for (DockingStation station : stations) {
			TileEntity sideTile = ((TileGenericPipe) tile).getTile(station.side);
			Block sideBlock = ((TileGenericPipe) tile).getBlock(station.side);

			if (sideTile instanceof IPipeTile) {
				continue;
			}

			if (station.getItemOutput() != null) {
				result.add(BuildCraftRobotics.actionStationProvideItems);
			}

			if (station.getFluidOutput() != null) {
				result.add(BuildCraftRobotics.actionStationProvideFluids);
			}

			if (station.getRequestProvider() != null) {
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
