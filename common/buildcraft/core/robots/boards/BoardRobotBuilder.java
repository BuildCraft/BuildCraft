/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.robots.boards;

import buildcraft.api.boards.RedstoneBoardRobot;
import buildcraft.api.boards.RedstoneBoardRobotNBT;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.builders.TileConstructionMarker;
import buildcraft.core.blueprints.BuildingSlot;
import buildcraft.core.robots.AIRobotGotoSleep;

public class BoardRobotBuilder extends RedstoneBoardRobot {

	private TileConstructionMarker markerToBuild;
	private BuildingSlot currentBuildingSlot;

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
		}
	}
}
