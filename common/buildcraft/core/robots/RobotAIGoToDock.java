/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.robots;

import buildcraft.robots.DockingStation;

public class RobotAIGoToDock extends RobotAIComposite {

	public RobotAIGoToDock(EntityRobot iRobot, DockingStation station) {
		super(iRobot,
				new RobotAIMoveTo(iRobot,
						station.pipe.xCoord + 0.5F + station.side.offsetX * 1.5F,
						station.pipe.yCoord + 0.5F + station.side.offsetY * 1.5F,
						station.pipe.zCoord + 0.5F + station.side.offsetZ * 1.5F),
				new RobotAIDirectMoveTo(iRobot,
						station.pipe.xCoord + 0.5F + station.side.offsetX * 0.5F,
						station.pipe.yCoord + 0.5F + station.side.offsetY * 0.5F,
						station.pipe.zCoord + 0.5F + station.side.offsetZ * 0.5F),
				new RobotAIDocked(iRobot, station));
	}
}
