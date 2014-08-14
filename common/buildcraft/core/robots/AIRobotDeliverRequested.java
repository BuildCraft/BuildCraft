/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.robots;

import net.minecraft.item.ItemStack;

import buildcraft.api.core.BlockIndex;
import buildcraft.api.core.IInvSlot;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.api.robots.IRequestProvider;
import buildcraft.api.robots.StackRequest;
import buildcraft.core.inventory.InvUtils;
import buildcraft.core.inventory.filters.ArrayStackFilter;
import buildcraft.silicon.statements.ActionStationRequestItemsMachine;
import buildcraft.transport.Pipe;
import buildcraft.transport.gates.ActionIterator;
import buildcraft.transport.gates.ActionSlot;

public class AIRobotDeliverRequested extends AIRobot {

	private StackRequest requested;
	private boolean delivered = false;

	public AIRobotDeliverRequested(EntityRobotBase iRobot) {
		super(iRobot);
	}

	public AIRobotDeliverRequested(EntityRobotBase robot, StackRequest request) {
		super(robot);

		requested = request;
	}

	@Override
	public void start() {
		startDelegateAI(new AIRobotGotoStation(robot, requested.station));
	}

	@Override
	public void delegateAIEnded(AIRobot ai) {
		if (ai instanceof AIRobotGotoStation) {
			if (!ai.success()) {
				terminate();
				return;
			}

			IInvSlot slot = InvUtils.getItem(robot, new ArrayStackFilter(requested.stack));

			if (slot == null) {
				terminate();
				return;
			}

			if (requested.requester != null) {
				ItemStack newStack = ((IRequestProvider)
					requested.requester).provideItemsForRequest(requested.index,
							slot.getStackInSlot().copy());

				if (newStack == null || newStack.stackSize != slot.getStackInSlot().stackSize) {
					delivered = true;
					slot.setStackInSlot(newStack);
				}

				terminate();
			} else {
				startDelegateAI(new AIRobotUnload(robot));
				return;
			}
		} else if (ai instanceof AIRobotUnload) {
			delivered = ai.success();
			terminate();
		}
	}


	@Override
	public boolean success() {
		return delivered;
	}

	private class StationProviderFilter implements IStationFilter {

		@Override
		public boolean matches(DockingStation station) {
			boolean actionFound = false;

			Pipe pipe = station.getPipe().pipe;

			if (!station.index().nextTo(new BlockIndex(requested.requester))) {
				return false;
			}

			for (ActionSlot s : new ActionIterator(pipe)) {
				if (s.action instanceof ActionStationRequestItemsMachine) {
					actionFound = true;
				}
			}

			if (!actionFound) {
				return false;
			}

			return true;
		}
	}
}
