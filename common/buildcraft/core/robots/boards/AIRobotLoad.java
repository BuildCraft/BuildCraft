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

import buildcraft.api.core.IInvSlot;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.core.inventory.ITransactor;
import buildcraft.core.inventory.InventoryIterator;
import buildcraft.core.inventory.Transactor;
import buildcraft.core.robots.DockingStation;

public class AIRobotLoad extends AIRobot {

	public AIRobotLoad(EntityRobotBase iRobot) {
		super(iRobot, 0, 1);
	}

	@Override
	public void start() {
		if (robot.getDockingStation() != null) {
			DockingStation station = (DockingStation) robot.getDockingStation();

			ItemStack itemFound = null;

			for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
				TileEntity nearbyTile = robot.worldObj.getTileEntity(station.pipe.xCoord + dir.offsetX,
						station.pipe.yCoord
								+ dir.offsetY, station.pipe.zCoord + dir.offsetZ);

				if (nearbyTile != null && nearbyTile instanceof IInventory) {
					IInventory tileInventory = (IInventory) nearbyTile;
					ITransactor robotTransactor = Transactor.getTransactorFor(robot);

					for (int i = 0; i < robot.getSizeInventory(); ++i) {
						if (robot.getStackInSlot(i) == null) {
							for (IInvSlot slot : InventoryIterator.getIterable(tileInventory, dir.getOpposite())) {
								ItemStack stack = slot.getStackInSlot();

								if (stack != null) {
									slot.setStackInSlot(null);
									robot.setInventorySlotContents(i, stack);
									break;
								}
							}
						}
					}
				}
			}
		}

		terminate();
	}
}
