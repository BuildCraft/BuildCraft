/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.robotics.ai;

import net.minecraft.item.ItemStack;

import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.api.core.IInvSlot;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.DockingStation;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.api.transport.IInjectable;
import buildcraft.core.lib.inventory.InventoryIterator;
import buildcraft.core.lib.inventory.filters.ArrayStackOrListFilter;
import buildcraft.robotics.statements.ActionRobotFilter;
import buildcraft.robotics.statements.ActionStationAcceptItems;

public class AIRobotUnload extends AIRobot {

	private int waitedCycles = 0;

	public AIRobotUnload(EntityRobotBase iRobot) {
		super(iRobot);
	}

	@Override
	public void update() {
		waitedCycles++;

		if (waitedCycles > 40) {
			if (unload(robot, robot.getDockingStation(), true)) {
				waitedCycles = 0;
			} else {
				setSuccess(!robot.containsItems());
				terminate();
			}
		}
	}

	public static boolean unload(EntityRobotBase robot, DockingStation station, boolean doUnload) {
		if (station == null) {
			return false;
		}

		IInjectable output = station.getItemOutput();
		if (output == null) {
			return false;
		}

		ForgeDirection injectSide = station.getItemOutputSide();
		if (!output.canInjectItems(injectSide)) {
			return false;
		}

		for (IInvSlot robotSlot : InventoryIterator.getIterable(robot, ForgeDirection.UNKNOWN)) {
			if (robotSlot.getStackInSlot() == null) {
				continue;
			}

			if (!ActionRobotFilter
					.canInteractWithItem(station, new ArrayStackOrListFilter(robotSlot.getStackInSlot()),
							ActionStationAcceptItems.class)) {
				continue;
			}

			ItemStack stack = robotSlot.getStackInSlot();
			int used = output.injectItem(stack, doUnload, injectSide, null);

			if (used > 0) {
				if (doUnload) {
					robotSlot.decreaseStackInSlot(used);
				}
				return true;
			}
		}

		if (robot.getHeldItem() != null) {
			if (!ActionRobotFilter
					.canInteractWithItem(station, new ArrayStackOrListFilter(robot.getHeldItem()),
							ActionStationAcceptItems.class)) {
				return false;
			}

			ItemStack stack = robot.getHeldItem();
			int used = output.injectItem(stack, doUnload, injectSide, null);

			if (used > 0) {
				if (doUnload) {
					if (stack.stackSize <= used) {
						robot.setItemInUse(null);
					} else {
						stack.stackSize -= used;
					}
				}
				return true;
			}
		}

		return false;
	}

	@Override
	public int getEnergyCost() {
		return 10;
	}
}
