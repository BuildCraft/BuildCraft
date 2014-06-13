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
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.DockingStationRegistry;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.api.robots.IDockingStation;
import buildcraft.core.inventory.ITransactor;
import buildcraft.core.inventory.Transactor;
import buildcraft.core.inventory.filters.IStackFilter;
import buildcraft.core.robots.AIRobotGoToDock;
import buildcraft.core.robots.DockingStation;

public class AIRobotFetchItemStack extends AIRobot {

	private DockingStation stationToDock = null;
	private IStackFilter filter;

	public AIRobotFetchItemStack(EntityRobotBase iRobot, IStackFilter iFilter) {
		super(iRobot);

		filter = iFilter;
	}

	@Override
	public void update() {
		for (IDockingStation d : DockingStationRegistry.getStations()) {
			DockingStation station = (DockingStation) d;

			if (station.reserved != null) {
				continue;
			}

			for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
				TileEntity nearbyTile = robot.worldObj.getTileEntity(d.x() + dir.offsetX, d.y()
						+ dir.offsetY, d.z()
						+ dir.offsetZ);

				if (nearbyTile != null && nearbyTile instanceof IInventory) {
					ITransactor trans = Transactor.getTransactorFor(nearbyTile);

					if (trans.remove(filter, dir.getOpposite(), false) != null) {
						stationToDock = station;
						startDelegateAI(new AIRobotGoToDock(robot, stationToDock));
						return;
					}
				}
			}
		}
	}

	@Override
	public void delegateAIEnded(AIRobot ai) {
		ItemStack itemFound = null;

		for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
			TileEntity nearbyTile = robot.worldObj.getTileEntity(stationToDock.pipe.xCoord + dir.offsetX,
					stationToDock.pipe.yCoord
							+ dir.offsetY, stationToDock.pipe.zCoord + dir.offsetZ);

			if (nearbyTile != null && nearbyTile instanceof IInventory) {
				ITransactor trans = Transactor.getTransactorFor(nearbyTile);

				itemFound = trans.remove(filter, dir.getOpposite(), true);

				if (itemFound != null) {
					break;
				}
			}
		}

		if (itemFound != null) {
			robot.setItemInUse(itemFound);
			terminate();
		}
	}
}
