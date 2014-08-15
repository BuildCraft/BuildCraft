/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.robots.boards;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.nbt.NBTTagCompound;

import buildcraft.api.boards.RedstoneBoardRobot;
import buildcraft.api.boards.RedstoneBoardRobotNBT;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.core.robots.AIRobotFetchItem;
import buildcraft.core.robots.AIRobotGotoSleep;
import buildcraft.core.robots.AIRobotGotoStationAndUnload;
import buildcraft.silicon.statements.ActionRobotFilter;

public class BoardRobotPicker extends RedstoneBoardRobot {

	// TODO: Clean this when world unloaded
	public static Set<Integer> targettedItems = new HashSet<Integer>();

	private NBTTagCompound data;

	public BoardRobotPicker(EntityRobotBase iRobot) {
		super(iRobot);
	}

	public BoardRobotPicker(EntityRobotBase robot, NBTTagCompound nbt) {
		super(robot);
		data = nbt;
	}

	@Override
	public void update() {
		startDelegateAI(new AIRobotFetchItem(robot, 250, ActionRobotFilter.getGateFilter(robot
				.getLinkedStation()), robot.getZoneToWork()));
	}

	@Override
	public void delegateAIEnded(AIRobot ai) {
		if (ai instanceof AIRobotFetchItem) {
			AIRobotFetchItem fetching = (AIRobotFetchItem) ai;

			if (fetching.itemPickupCancelled || fetching.target != null) {
				// if we find an item - that may have been cancelled.
				// let's try to get another one
				startDelegateAI(new AIRobotFetchItem(robot, 250, ActionRobotFilter.getGateFilter(robot
						.getLinkedStation()), robot.getZoneToWork()));
			} else if (robot.containsItems()) {
				startDelegateAI(new AIRobotGotoStationAndUnload(robot, robot.getZoneToWork()));
			} else {
				startDelegateAI(new AIRobotGotoSleep(robot));
			}
		} else if (ai instanceof AIRobotGotoStationAndUnload) {
			if (!ai.success()) {
				startDelegateAI(new AIRobotGotoSleep(robot));
			}
		}
	}

	@Override
	public RedstoneBoardRobotNBT getNBTHandler() {
		return BoardRobotPickerNBT.instance;
	}

	@Override
	public void writeSelfToNBT(NBTTagCompound nbt) {
		super.writeSelfToNBT(nbt);
	}

	@Override
	public void loadSelfFromNBT(NBTTagCompound nbt) {
		super.loadSelfFromNBT(nbt);
	}
}
