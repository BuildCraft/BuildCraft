/**
 * Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.robotics.ai;

import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.TileEntity;

import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.api.core.IZone;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.core.lib.inventory.ITransactor;
import buildcraft.core.lib.inventory.Transactor;
import buildcraft.core.lib.inventory.filters.IStackFilter;
import buildcraft.robotics.DockingStation;
import buildcraft.robotics.IStationFilter;
import buildcraft.robotics.statements.ActionRobotFilter;
import buildcraft.robotics.statements.ActionStationProvideItems;

public class AIRobotGotoStationToLoad extends AIRobot {

	private boolean found = false;
	private IStackFilter filter;
	private IZone zone;

	public AIRobotGotoStationToLoad(EntityRobotBase iRobot) {
		super(iRobot);
	}

	public AIRobotGotoStationToLoad(EntityRobotBase iRobot, IStackFilter iFilter, IZone iZone) {
		super(iRobot);

		filter = iFilter;
		zone = iZone;
	}

	@Override
	public void update() {
		startDelegateAI(new AIRobotSearchAndGotoStation(robot, new StationFilter(), zone));
	}

	@Override
	public void delegateAIEnded(AIRobot ai) {
		if (ai instanceof AIRobotSearchAndGotoStation) {
			found = ((AIRobotSearchAndGotoStation) ai).targetStation != null;

			terminate();
		}
	}

	@Override
	public boolean success() {
		return found;
	}

	private class StationFilter implements IStationFilter {

		@Override
		public boolean matches(DockingStation station) {
			if (!ActionRobotFilter.canInteractWithItem(station, filter, ActionStationProvideItems.class)) {
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
