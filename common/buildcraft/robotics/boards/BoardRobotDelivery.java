/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.robotics.boards;

import java.util.ArrayList;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import buildcraft.api.boards.RedstoneBoardRobot;
import buildcraft.api.boards.RedstoneBoardRobotNBT;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.core.lib.inventory.StackHelper;
import buildcraft.core.lib.inventory.filters.IStackFilter;
import buildcraft.robotics.StackRequest;
import buildcraft.robotics.ai.AIRobotDeliverRequested;
import buildcraft.robotics.ai.AIRobotDisposeItems;
import buildcraft.robotics.ai.AIRobotGotoSleep;
import buildcraft.robotics.ai.AIRobotGotoStationAndLoad;
import buildcraft.robotics.ai.AIRobotSearchStackRequest;
import buildcraft.robotics.statements.ActionRobotFilter;

public class BoardRobotDelivery extends RedstoneBoardRobot {

	private ArrayList<ItemStack> deliveryBlacklist = new ArrayList<ItemStack>();

	private StackRequest currentRequest = null;

	public BoardRobotDelivery(EntityRobotBase iRobot) {
		super(iRobot);
	}

	@Override
	public RedstoneBoardRobotNBT getNBTHandler() {
		return BCBoardNBT.REGISTRY.get("delivery");
	}

	@Override
	public void update() {
		if (robot.containsItems()) {
			startDelegateAI(new AIRobotDisposeItems(robot));
			return;
		}

		if (currentRequest == null) {
			startDelegateAI(new AIRobotSearchStackRequest(robot, ActionRobotFilter.getGateFilter(robot
					.getLinkedStation()), deliveryBlacklist));
		} else {
			startDelegateAI(new AIRobotGotoStationAndLoad(robot, new IStackFilter() {
				@Override
				public boolean matches(ItemStack stack) {
					return currentRequest != null && StackHelper.isMatchingItemOrList(stack, currentRequest.getStack());
				}
			}, currentRequest.getStack().stackSize));
		}
	}

	@Override
	public void delegateAIEnded(AIRobot ai) {
		if (ai instanceof AIRobotSearchStackRequest) {
			if (!ai.success()) {
				deliveryBlacklist.clear();
				startDelegateAI(new AIRobotGotoSleep(robot));
			} else {
				currentRequest = ((AIRobotSearchStackRequest) ai).request;

				if (!currentRequest.getStation(robot.worldObj).take(robot)) {
					releaseCurrentRequest();
				}
			}
		} else if (ai instanceof AIRobotGotoStationAndLoad) {
			if (!ai.success()) {
				deliveryBlacklist.add(currentRequest.getStack());
				releaseCurrentRequest();
			} else {
				startDelegateAI(new AIRobotDeliverRequested(robot, currentRequest));
			}
		} else if (ai instanceof AIRobotDeliverRequested) {
			releaseCurrentRequest();
		}
	}

	private void releaseCurrentRequest() {
		if (currentRequest != null) {
			robot.getRegistry().release(currentRequest.getResourceId(robot.worldObj));
			currentRequest.getStation(robot.worldObj).release(robot);
			currentRequest = null;
		}
	}

	@Override
	public boolean canLoadFromNBT() {
		return true;
	}

	@Override
	public void writeSelfToNBT(NBTTagCompound nbt) {
		super.writeSelfToNBT(nbt);

		if (currentRequest != null) {
			NBTTagCompound requestNBT = new NBTTagCompound();
			currentRequest.writeToNBT(requestNBT);
			nbt.setTag("currentRequest", requestNBT);
		}
	}

	@Override
	public void loadSelfFromNBT(NBTTagCompound nbt) {
		super.loadSelfFromNBT(nbt);
		if (nbt.hasKey("currentRequest")) {
			currentRequest = StackRequest.loadFromNBT(nbt.getCompoundTag("currentRequest"));
		}
	}
}
