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
import net.minecraft.nbt.NBTTagCompound;

import buildcraft.api.core.IInvSlot;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.api.robots.IRequestProvider;
import buildcraft.core.lib.inventory.InvUtils;
import buildcraft.core.lib.inventory.filters.ArrayStackOrListFilter;
import buildcraft.robotics.StackRequest;

public class AIRobotDeliverRequested extends AIRobot {

	private StackRequest requested;
	private boolean delivered = false;

	public AIRobotDeliverRequested(EntityRobotBase iRobot) {
		super(iRobot);
	}

	public AIRobotDeliverRequested(EntityRobotBase robot, StackRequest request) {
		this(robot);

		requested = request;
	}

	@Override
	public void start() {
		if (requested != null) {
			startDelegateAI(new AIRobotGotoStation(robot, requested.getStation(robot.worldObj)));
		} else {
			setSuccess(false);
			terminate();
		}
	}

	@Override
	public void delegateAIEnded(AIRobot ai) {
		if (ai instanceof AIRobotGotoStation) {
			if (!ai.success()) {
				setSuccess(false);
				terminate();
				return;
			}

			IRequestProvider requester = requested.getRequester(robot.worldObj);
			if (requester == null) {
				setSuccess(false);
				terminate();
				return;
			}

			// TODO: Make this not exceed the requested amount of items.

			int count = 0;

			for (IInvSlot slot : InvUtils.getItems(robot, new ArrayStackOrListFilter(requested.getStack()))) {
				int difference = slot.getStackInSlot().stackSize;
				ItemStack newStack = requester.offerItem(requested.getSlot(), slot.getStackInSlot().copy());

				if (newStack == null) {
					slot.setStackInSlot(newStack);
				} else if (newStack.stackSize != slot.getStackInSlot().stackSize) {
					slot.setStackInSlot(newStack);
					difference = newStack.stackSize - difference;
				}

				count += difference;
			}

			setSuccess(count > 0);
			terminate();
		}
	}

	@Override
	public boolean success() {
		return delivered;
	}

	@Override
	public boolean canLoadFromNBT() {
		return true;
	}

	@Override
	public void writeSelfToNBT(NBTTagCompound nbt) {
		super.writeSelfToNBT(nbt);

		if (requested != null) {
			NBTTagCompound requestNBT = new NBTTagCompound();
			requested.writeToNBT(requestNBT);
			nbt.setTag("currentRequest", requestNBT);
		}
	}

	@Override
	public void loadSelfFromNBT(NBTTagCompound nbt) {
		super.loadSelfFromNBT(nbt);
		if (nbt.hasKey("currentRequest")) {
			requested = StackRequest.loadFromNBT(nbt.getCompoundTag("currentRequest"));
		}
	}
}
