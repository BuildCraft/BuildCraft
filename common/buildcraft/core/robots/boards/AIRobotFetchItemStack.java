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

import buildcraft.core.inventory.ITransactor;
import buildcraft.core.inventory.Transactor;
import buildcraft.core.inventory.filters.IStackFilter;
import buildcraft.core.robots.AIRobotGoToDock;
import buildcraft.robots.AIRobot;
import buildcraft.robots.DockingStation;
import buildcraft.robots.DockingStationRegistry;
import buildcraft.robots.EntityRobotBase;

public class AIRobotFetchItemStack extends AIRobot {

	private DockingStation station = null;
	private IStackFilter filter;

	public AIRobotFetchItemStack(EntityRobotBase iRobot, IStackFilter iFilter) {
		super(iRobot);

		filter = iFilter;
	}

	@Override
	public void update() {
		for (DockingStation d : DockingStationRegistry.getStations()) {
			if (d.reserved != null) {
				continue;
			}

			for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
				TileEntity nearbyTile = robot.worldObj.getTileEntity(d.pipe.xCoord + dir.offsetX, d.pipe.yCoord
						+ dir.offsetY, d.pipe.zCoord
						+ dir.offsetZ);

				if (nearbyTile != null && nearbyTile instanceof IInventory) {
					ITransactor trans = Transactor.getTransactorFor(nearbyTile);

					if (trans.remove(filter, dir.getOpposite(), false) != null) {
						station = d;
						startDelegateAI(new AIRobotGoToDock(robot, station));
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
			TileEntity nearbyTile = robot.worldObj.getTileEntity(station.pipe.xCoord + dir.offsetX,
					station.pipe.yCoord
							+ dir.offsetY, station.pipe.zCoord + dir.offsetZ);

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
