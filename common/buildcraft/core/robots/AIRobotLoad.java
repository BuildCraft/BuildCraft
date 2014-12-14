/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.robots;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

import net.minecraft.util.EnumFacing;

import buildcraft.api.core.IInvSlot;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.core.inventory.ITransactor;
import buildcraft.core.inventory.InventoryIterator;
import buildcraft.core.inventory.Transactor;
import buildcraft.core.inventory.filters.IStackFilter;
import buildcraft.silicon.statements.ActionRobotFilter;
import buildcraft.silicon.statements.ActionStationProvideItems;

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

			for (EnumFacing dir : EnumFacing.values()) {
				TileEntity nearbyTile = robot.worldObj.getTileEntity(station.pos().offset(dir));

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

									ItemStack added = t.add(slot.getStackInSlot(), null, true);
									slot.decreaseStackInSlot(added.stackSize);
									return;
								} else {
									ItemStack toAdd = slot.getStackInSlot().copy();

									if (toAdd.stackSize >= quantity) {
										toAdd.stackSize = quantity;
									}

									ItemStack added = t.add(toAdd, null, true);
									slot.decreaseStackInSlot(added.stackSize);
									return;
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
		return 20;
	}
}
