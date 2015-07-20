/**
 * Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
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
		startDelegateAI(new AIRobotGotoStation(robot, requested.getStation(robot.worldObj)));
	}

	@Override
	public void delegateAIEnded(AIRobot ai) {
		if (ai instanceof AIRobotGotoStation) {
			if (!ai.success()) {
				setSuccess(false);
				terminate();
				return;
			}

			IInvSlot slot = InvUtils.getItem(robot, new ArrayStackOrListFilter(requested.getStack()));

			if (slot == null) {
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
			ItemStack newStack = requester.offerItem(requested.getSlot(), slot.getStackInSlot().copy());

			if (newStack == null || newStack.stackSize != slot.getStackInSlot().stackSize) {
				slot.setStackInSlot(newStack);
			}
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
