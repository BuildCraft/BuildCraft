/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.robots.boards;

import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.TileEntity;

import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.core.inventory.ITransactor;
import buildcraft.core.inventory.Transactor;
import buildcraft.core.inventory.filters.IStackFilter;
import buildcraft.core.robots.AIRobotLookForStation;
import buildcraft.core.robots.DockingStation;
import buildcraft.core.robots.IStationFilter;
import buildcraft.silicon.statements.StateStationProvideItems;
import buildcraft.transport.Pipe;

public class AIRobotGotoItemStack extends AIRobot {

	private IStackFilter filter;

	public AIRobotGotoItemStack(EntityRobotBase iRobot, IStackFilter iFilter) {
		super(iRobot, 0, 1);

		filter = iFilter;
	}

	@Override
	public void update() {
		startDelegateAI(new AIRobotLookForStation(robot, new StationFilter()));
	}

	@Override
	public void delegateAIEnded(AIRobot ai) {
		terminate();
	}

	private class StationFilter implements IStationFilter {

		@Override
		public boolean matches(DockingStation station) {
			boolean found = false;

			Pipe pipe = station.pipe.pipe;

			for (Object s : pipe.getActionStates()) {
				if (s instanceof StateStationProvideItems) {
					if (((StateStationProvideItems) s).matches(filter)) {
						found = true;
						break;
					}
				}
			}

			if (!found) {
				return false;
			}

			for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
				TileEntity nearbyTile = robot.worldObj.getTileEntity(station.x() + dir.offsetX, station.y()
						+ dir.offsetY, station.z()
						+ dir.offsetZ);

				if (nearbyTile != null && nearbyTile instanceof IInventory) {
					ITransactor trans = Transactor.getTransactorFor(nearbyTile);

					if (trans.remove(filter, dir.getOpposite(), false) != null) {
						return true;
					}
				}
			}

			return false;
		}

	}
}
