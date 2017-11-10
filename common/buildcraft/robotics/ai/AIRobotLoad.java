/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.robotics.ai;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.api.core.IInvSlot;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.DockingStation;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.core.lib.inventory.ITransactor;
import buildcraft.core.lib.inventory.InventoryIterator;
import buildcraft.core.lib.inventory.Transactor;
import buildcraft.core.lib.inventory.filters.IStackFilter;
import buildcraft.robotics.statements.ActionRobotFilter;
import buildcraft.robotics.statements.ActionStationProvideItems;

public class AIRobotLoad extends AIRobot {

	public static final int ANY_QUANTITY = -1;
	private IStackFilter filter;
	private int quantity;
	private int waitedCycles = 0;

	public AIRobotLoad(EntityRobotBase iRobot) {
		super(iRobot);
	}

	public AIRobotLoad(EntityRobotBase iRobot, IStackFilter iFilter, int iQuantity) {
		super(iRobot);

		filter = iFilter;
		quantity = iQuantity;
	}

	@Override
	public void update() {
		if (filter == null) {
			terminate();
			return;
		}

		waitedCycles++;

		if (waitedCycles > 40) {
			setSuccess(load(robot, robot.getDockingStation(), filter, quantity, true));
			terminate();
		}
	}

	/** Similar method to {@link #load(EntityRobotBase, DockingStation, IStackFilter, int, boolean)} but returns the
     * itemstack rather than loading it onto the robot.
     * 
     * Only loads a single stack at once. */
	public static ItemStack takeSingle(DockingStation station, IStackFilter filter, boolean doTake) {
        if (station == null) {
            return null;
        }

        IInventory tileInventory = station.getItemInput();
        if (tileInventory == null) {
            return null;
        }

        for (IInvSlot slot : InventoryIterator.getIterable(tileInventory, station.getItemInputSide())) {
            ItemStack stack = slot.getStackInSlot();

            if (stack == null
                    || !slot.canTakeStackFromSlot(stack)
                    || !filter.matches(stack)
                    || !ActionStationProvideItems.canExtractItem(station, stack)
                    || !ActionRobotFilter.canInteractWithItem(station, filter,
                    ActionStationProvideItems.class)) {
                continue;
            }

                if (doTake) {
                   stack = slot.decreaseStackInSlot(1);
                } else {
                    stack = stack.copy();
                    stack = stack.splitStack(1);
                }
                return stack;
        }
        return null;
    }

	public static boolean load(EntityRobotBase robot, DockingStation station, IStackFilter filter,
							   int quantity, boolean doLoad) {
		if (station == null) {
			return false;
		}

		int loaded = 0;

		IInventory tileInventory = station.getItemInput();
		if (tileInventory == null) {
			return false;
		}

		for (IInvSlot slot : InventoryIterator.getIterable(tileInventory, station.getItemInputSide())) {
			ItemStack stack = slot.getStackInSlot();

			if (stack == null
					|| !slot.canTakeStackFromSlot(stack)
					|| !filter.matches(stack)
					|| !ActionStationProvideItems.canExtractItem(station, stack)
					|| !ActionRobotFilter.canInteractWithItem(station, filter,
					ActionStationProvideItems.class)) {
				continue;
			}

			ITransactor robotTransactor = Transactor.getTransactorFor(robot);

			if (quantity == ANY_QUANTITY) {
				ItemStack added = robotTransactor.add(slot.getStackInSlot(),
						ForgeDirection.UNKNOWN, doLoad);
				if (doLoad) {
					slot.decreaseStackInSlot(added.stackSize);
				}
				return added.stackSize > 0;
			} else {
				ItemStack toAdd = slot.getStackInSlot().copy();

				if (toAdd.stackSize > quantity - loaded) {
					toAdd.stackSize = quantity - loaded;
				}

				ItemStack added = robotTransactor.add(toAdd, ForgeDirection.UNKNOWN, doLoad);
				if (doLoad) {
					slot.decreaseStackInSlot(added.stackSize);
				}
				loaded += added.stackSize;

				if (quantity - loaded <= 0) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public int getEnergyCost() {
		return 8;
	}
}
