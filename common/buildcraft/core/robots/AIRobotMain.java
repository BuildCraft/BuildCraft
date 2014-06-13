/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.robots;

import buildcraft.api.boards.RedstoneBoardRobot;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;

public class AIRobotMain extends AIRobot {

	public AIRobotMain(EntityRobotBase iRobot) {
		super(iRobot);
	}

	@Override
	public void start() {
		startDelegateAI(robot.getBoard());
	}

	@Override
	public void preempt(AIRobot ai) {
		if (ai instanceof RedstoneBoardRobot) {
			if (robot.getEnergy() < EntityRobotBase.MAX_ENERGY / 4.0) {
				abortDelegateAI();
				startDelegateAI(new AIRobotRecharge(robot));
			}
		}
	}

	@Override
	public void delegateAIEnded(AIRobot ai) {
		if (ai instanceof AIRobotRecharge) {
			startDelegateAI(robot.getBoard());
		}
	}

}
