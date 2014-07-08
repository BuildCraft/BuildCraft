/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.robots.boards;

import java.util.LinkedList;

import net.minecraft.item.ItemStack;

import buildcraft.api.boards.RedstoneBoardRobot;
import buildcraft.api.boards.RedstoneBoardRobotNBT;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.builders.TileConstructionMarker;
import buildcraft.core.blueprints.BuildingSlot;
import buildcraft.core.inventory.filters.ArrayStackFilter;
import buildcraft.core.robots.AIRobotGotoBlock;
import buildcraft.core.robots.AIRobotGotoSleep;
import buildcraft.core.robots.AIRobotGotoStationToLoad;
import buildcraft.core.robots.AIRobotLoad;

public class BoardRobotBuilder extends RedstoneBoardRobot {

	private TileConstructionMarker markerToBuild;
	private BuildingSlot currentBuildingSlot;
	private LinkedList<ItemStack> requirementsToLookFor;

	public BoardRobotBuilder(EntityRobotBase iRobot) {
		super(iRobot);
	}

	@Override
	public RedstoneBoardRobotNBT getNBTHandler() {
		return BoardRobotBuilderNBT.instance;
	}

	@Override
	public void update() {
		if (markerToBuild == null) {
			double minDistance = Double.MAX_VALUE;

			for (TileConstructionMarker marker : TileConstructionMarker.currentMarkers) {
				if (marker.getWorld() == robot.worldObj && marker.needsToBuild()) {
					double dx = robot.posX - marker.xCoord;
					double dy = robot.posY - marker.yCoord;
					double dz = robot.posZ - marker.zCoord;
					double distance = dx * dx + dy * dy + dz * dz;

					if (distance < minDistance) {
						markerToBuild = marker;
						minDistance = distance;
					}
				}
			}

			if (markerToBuild == null) {
				startDelegateAI(new AIRobotGotoSleep(robot));
				return;
			}
		}

		if (markerToBuild == null || !markerToBuild.needsToBuild()) {
			markerToBuild = null;
			startDelegateAI(new AIRobot(robot));
			return;
		}

		if (currentBuildingSlot == null) {
			currentBuildingSlot = markerToBuild.bluePrintBuilder.reserveNextSlot(robot.worldObj);

			if (currentBuildingSlot != null) {
				requirementsToLookFor = currentBuildingSlot.getRequirements(markerToBuild.getContext());
			} else {
				startDelegateAI(new AIRobotGotoSleep(robot));
				return;
			}

			// TODO: what if there's more requirements that this robot can
			// handle e.g. not enough free spots?
		}

		if (requirementsToLookFor != null && requirementsToLookFor.size() > 0) {
			startDelegateAI(new AIRobotGotoStationToLoad(robot,
					new ArrayStackFilter(requirementsToLookFor.getFirst()),
					robot.getAreaToWork()));
		}

		if (currentBuildingSlot != null && requirementsToLookFor != null && requirementsToLookFor.size() == 0) {
			// TODO: It's probably OK to get at least X units away from
			// destination. something to handle at the path-finding level (e.g.
			// end computation) to be brought up to here as a parameter.
			startDelegateAI(new AIRobotGotoBlock(robot,
					(int) currentBuildingSlot.getDestination().x,
					(int) currentBuildingSlot.getDestination().y,
					(int) currentBuildingSlot.getDestination().z));
		}
	}

	@Override
	public void delegateAIEnded(AIRobot ai) {
		if (ai instanceof AIRobotGotoStationToLoad) {
			if (((AIRobotGotoStationToLoad) ai).found) {
				// TODO: How to load only the required amount of items there?
				startDelegateAI(new AIRobotLoad(robot, new ArrayStackFilter(requirementsToLookFor.getFirst())));
			} else {
				startDelegateAI(new AIRobotGotoSleep(robot));
			}
		} else if (ai instanceof AIRobotLoad) {
			// TODO: check that we get the proper items in
			requirementsToLookFor.removeFirst();
		} else if (ai instanceof AIRobotGotoBlock) {
			// TODO: here we want to start the animation, and to update the
			// builder state (remove slot from list, add to post processing,
			// etc);
			currentBuildingSlot.writeToWorld(markerToBuild.getContext());
			currentBuildingSlot = null;
			requirementsToLookFor = null;
		}
	}
}
