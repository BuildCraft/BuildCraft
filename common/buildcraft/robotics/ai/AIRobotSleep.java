/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.robotics.ai;

import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.api.statements.StatementSlot;
import buildcraft.robotics.statements.ActionRobotWakeUp;

public class AIRobotSleep extends AIRobot {

	private static final int SLEEPING_TIME = 60 * 20;
	private int sleptTime = 0;

	public AIRobotSleep(EntityRobotBase iRobot) {
		super(iRobot);
	}

	@Override
	public void preempt(AIRobot ai) {
		for (StatementSlot s : robot.getLinkedStation().getActiveActions()) {
			if (s.statement instanceof ActionRobotWakeUp) {
				terminate();
			}
		}
	}

	@Override
	public void update() {
		sleptTime++;

		if (sleptTime > SLEEPING_TIME) {
			terminate();
		}
	}

	@Override
	public int getEnergyCost() {
		// This trick is so we get 0.1 RF per tick.
		return sleptTime % 10 == 0 ? 1 : 0;
	}
}
