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

import buildcraft.api.boards.RedstoneBoardRobot;
import buildcraft.api.boards.RedstoneBoardRobotNBT;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.core.inventory.ITransactor;
import buildcraft.core.inventory.Transactor;
import buildcraft.core.inventory.filters.PassThroughStackFilter;
import buildcraft.core.robots.DockingStation;

public class BoardRobotCarrier extends RedstoneBoardRobot {

	public BoardRobotCarrier(EntityRobotBase iRobot) {
		super(iRobot, 0);
	}

	@Override
	public RedstoneBoardRobotNBT getNBTHandler() {
		return BoardRobotCarrierNBT.instance;
	}

	@Override
	public void update() {
		boolean containItems = false;

		for (int i = 0; i < robot.getSizeInventory(); ++i) {
			if (robot.getStackInSlot(i) != null) {
				containItems = true;
			}
		}

		if (!containItems) {
			startDelegateAI(new AIRobotGotoItemStack(robot, new PassThroughStackFilter()));
		}
	}

	@Override
	public void delegateAIEnded(AIRobot ai) {
		if (ai instanceof AIRobotGotoItemStack) {
			load();
		}
	}

	private void load() {
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
							for (int j = 0; j < robot.getSizeInventory(); ++j) {
								ItemStack stack = tileInventory.getStackInSlot(j);

								if (tileInventory.getStackInSlot(j) != null) {
									tileInventory.setInventorySlotContents(j, null);
									robot.setInventorySlotContents(i, stack);
								}
							}
						}
					}
				}
			}
		}
	}

	private void unload() {

	}

}
