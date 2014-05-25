/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.robots;

public class RobotAIReturnToDock extends RobotAIComposite {

	public RobotAIReturnToDock(EntityRobot iRobot) {
		super(iRobot,
				new RobotAIMoveTo(iRobot,
						iRobot.dockingStation.x + 0.5F + iRobot.dockingStation.side.offsetX * 1.5F,
						iRobot.dockingStation.y + 0.5F + iRobot.dockingStation.side.offsetY * 1.5F,
						iRobot.dockingStation.z + 0.5F + iRobot.dockingStation.side.offsetZ * 1.5F),
				new RobotAIDirectMoveTo(iRobot,
						iRobot.dockingStation.x + 0.5F + iRobot.dockingStation.side.offsetX * 0.5F,
						iRobot.dockingStation.y + 0.5F + iRobot.dockingStation.side.offsetY * 0.5F,
						iRobot.dockingStation.z + 0.5F + iRobot.dockingStation.side.offsetZ * 0.5F),
				new RobotAIDocked(iRobot));
	}
}
