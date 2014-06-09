/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.robots.boards;

import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.core.inventory.ITransactor;
import buildcraft.core.inventory.Transactor;
import buildcraft.core.inventory.filters.ArrayStackFilter;
import buildcraft.core.robots.AIRobotGoToDock;
import buildcraft.robots.AIRobot;
import buildcraft.robots.DockingStation;
import buildcraft.robots.DockingStationRegistry;
import buildcraft.robots.EntityRobotBase;

public class AIRobotFetchAxe extends AIRobot {

	private DockingStation axeDocking = null;

	public AIRobotFetchAxe(EntityRobotBase iRobot) {
		super(iRobot);
	}

	@Override
	public void update() {
		for (DockingStation d : DockingStationRegistry.getStations()) {
			for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
				TileEntity nearbyTile = robot.worldObj.getTileEntity(d.pipe.xCoord + dir.offsetX, d.pipe.yCoord
						+ dir.offsetY, d.pipe.zCoord
						+ dir.offsetZ);

				if (nearbyTile != null && nearbyTile instanceof IInventory) {
					ArrayStackFilter filter = new ArrayStackFilter(new ItemStack(Items.wooden_axe));
					ITransactor trans = Transactor.getTransactorFor(nearbyTile);

					if (trans.remove(filter, dir.getOpposite(), false) != null) {
						axeDocking = d;
						startDelegateAI(new AIRobotGoToDock(robot, axeDocking));
						return;
					}
				}
			}
		}
	}

	@Override
	public void delegateAIEnded(AIRobot ai) {
		ItemStack axeFound = null;

		for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
			TileEntity nearbyTile = robot.worldObj.getTileEntity(axeDocking.pipe.xCoord + dir.offsetX,
					axeDocking.pipe.yCoord
							+ dir.offsetY, axeDocking.pipe.zCoord + dir.offsetZ);

			if (nearbyTile != null && nearbyTile instanceof IInventory) {
				ArrayStackFilter filter = new ArrayStackFilter(new ItemStack(Items.wooden_axe));
				ITransactor trans = Transactor.getTransactorFor(nearbyTile);

				axeFound = trans.remove(filter, dir.getOpposite(), true);

				if (axeFound != null) {
					break;
				}
			}
		}

		if (axeFound != null) {
			robot.setItemInUse(axeFound);
			terminate();
		}
	}
}
