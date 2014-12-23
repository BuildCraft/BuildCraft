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
import net.minecraft.block.BlockWorkbench;
import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.IFluidHandler;
import buildcraft.BuildCraftSilicon;
import buildcraft.api.robots.IRequestProvider;
import buildcraft.api.statements.IActionExternal;
import buildcraft.api.statements.IActionInternal;
import buildcraft.api.statements.IActionProvider;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.core.robots.DockingStation;
import buildcraft.silicon.TileAssemblyTable;
import buildcraft.transport.PipeTransportItems;
import buildcraft.transport.TileGenericPipe;

public class RobotsActionProvider implements IActionProvider {

	@Override
	public Collection<IActionInternal> getInternalActions(IStatementContainer container) {
		TileEntity tile = container.getTile();
		
		if (!(tile instanceof TileGenericPipe)) {
			return null;
		}
		
		LinkedList<IActionInternal> result = new LinkedList<IActionInternal>();

		ArrayList<DockingStation> stations = new ArrayList<DockingStation>();

		for (EnumFacing dir : EnumFacing.values()) {
			if (((TileGenericPipe) tile).getStation(dir) != null) {
				stations.add(((TileGenericPipe) tile).getStation(dir));
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

		if (((TileGenericPipe) tile).pipe.transport instanceof PipeTransportItems) {
			result.add(BuildCraftSilicon.actionStationDropInPipe);
		}

		for (EnumFacing dir : EnumFacing.values()) {
			TileEntity sideTile = ((TileGenericPipe) tile).getTile(dir);
			Block sideBlock = ((TileGenericPipe) tile).getBlock(dir);
			
			if (sideTile instanceof IInventory) {
				result.add(BuildCraftSilicon.actionStationProvideItems);
				result.add(BuildCraftSilicon.actionStationRequestItems);
				result.add(BuildCraftSilicon.actionStationAcceptItems);
			}

			if (sideTile instanceof IFluidHandler) {
				result.add(BuildCraftSilicon.actionStationAcceptFluids);
				result.add(BuildCraftSilicon.actionStationProvideFluids);
			}

			if (sideTile instanceof IRequestProvider) {
				result.add(BuildCraftSilicon.actionStationMachineRequestItems);
			}

			if (sideTile instanceof TileEntityFurnace
					|| sideTile instanceof TileAssemblyTable
					|| sideBlock instanceof BlockWorkbench) {
				result.add(BuildCraftSilicon.actionRobotAllowCraft);
			}
		}

		return result;
	}

	@Override
	public Collection<IActionExternal> getExternalActions(EnumFacing side, TileEntity tile) {
		return null;
	}

}
