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
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.api.core.IInvSlot;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.core.lib.inventory.ITransactor;
import buildcraft.core.lib.inventory.InventoryIterator;
import buildcraft.core.lib.inventory.Transactor;
import buildcraft.core.lib.inventory.filters.IStackFilter;
import buildcraft.robotics.DockingStation;
import buildcraft.robotics.statements.ActionRobotFilter;
import buildcraft.robotics.statements.ActionStationProvideItems;

public class AIRobotLoad extends AIRobot {

	private IStackFilter filter;
	private int quantity = -1;
	private int waitedCycles = 0;

	public AIRobotLoad(EntityRobotBase iRobot) {
		super(iRobot);
	}

	public AIRobotLoad(EntityRobotBase iRobot, IStackFilter iFilter) {
		super(iRobot);

		filter = iFilter;
	}

	public AIRobotLoad(EntityRobotBase iRobot, IStackFilter iFilter, int iQuantity) {
		super(iRobot);

		filter = iFilter;
		quantity = iQuantity;
	}

	@Override
	public void update() {
		if (filter == null) {
			// loading error
			terminate();
			return;
		}

		waitedCycles++;

		if (waitedCycles > 40) {
			doLoad();
			terminate();
		}
	}

	private void doLoad() {
		if (robot.getDockingStation() != null) {
			DockingStation station = (DockingStation) robot.getDockingStation();

			int loaded = 0;

			for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
				TileEntity nearbyTile = robot.worldObj.getTileEntity(station.x() + dir.offsetX,
						station.y()
								+ dir.offsetY, station.z() + dir.offsetZ);

				if (nearbyTile != null && nearbyTile instanceof IInventory) {
					IInventory tileInventory = (IInventory) nearbyTile;
					ITransactor robotTransactor = Transactor.getTransactorFor(robot);

					for (IInvSlot slot : InventoryIterator.getIterable(tileInventory, dir.getOpposite())) {
						ItemStack stack = slot.getStackInSlot();

						if (stack != null) {
							if (ActionRobotFilter.canInteractWithItem(station, filter, ActionStationProvideItems.class)
									&& filter.matches(stack)) {

								ITransactor t = Transactor.getTransactorFor(robot);

								if (quantity == -1) {
									ItemStack added = t.add(slot.getStackInSlot(), ForgeDirection.UNKNOWN, true);
									slot.decreaseStackInSlot(added.stackSize);
								} else {
									ItemStack toAdd = slot.getStackInSlot().copy();

									if (toAdd.stackSize > quantity - loaded) {
										toAdd.stackSize = quantity - loaded;
									}

									ItemStack added = t.add(toAdd, ForgeDirection.UNKNOWN, true);
									slot.decreaseStackInSlot(added.stackSize);
									loaded += added.stackSize;

									if (quantity - loaded <= 0) {
										return;
									}
								}
							}
						}
					}
				}
			}
		}
	}

	@Override
	public int getEnergyCost() {
		return 8;
	}
}
