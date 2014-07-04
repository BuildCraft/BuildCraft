/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.robots;

import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;

public class AIRobotGotoSleep extends AIRobot {

	public AIRobotGotoSleep(EntityRobotBase iRobot) {
		super(iRobot);
	}

	@Override
	public void start() {
		startDelegateAI(new AIRobotGotoDock(robot, robot.getLinkedStation()));
	}

	@Override
	public void delegateAIEnded(AIRobot ai) {
		if (ai instanceof AIRobotGotoDock) {
			startDelegateAI(new AIRobotSleep(robot));
		} else if (ai instanceof AIRobotSleep) {
			terminate();
		}
	}
}
