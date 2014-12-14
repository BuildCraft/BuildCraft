/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.robots;

import net.minecraft.entity.Entity;

import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;

public class AIRobotAttack extends AIRobot {

	public Entity target;

	private int delay = 10;

	public AIRobotAttack(EntityRobotBase iRobot) {
		super(iRobot);
	}

	public AIRobotAttack(EntityRobotBase iRobot, Entity iTarget) {
		super(iRobot);

		target = iTarget;
	}

	@Override
	public void preempt(AIRobot ai) {
		if (ai instanceof AIRobotGotoBlock) {
			// target may become null in the event of a load. In that case, just
			// go to the expected location.
			if (target != null && robot.getDistanceToEntity(target) <= 2.0) {
				abortDelegateAI();
				robot.setItemActive(true);
			}
		}
	}

	@Override
	public void update() {
		if (target.isDead) {
			terminate();
			return;
		}

		if (robot.getDistanceToEntity(target) > 2.0) {
			startDelegateAI(new AIRobotGotoBlock(robot, target.getPosition()));
			robot.setItemActive(false);

			return;
		}

		delay++;

		if (delay > 20) {
			delay = 0;
			((EntityRobot) robot).attackTargetEntityWithCurrentItem(target);
			robot.aimItemAt(target.getPosition());
		}
	}

	@Override
	public void end() {
		robot.setItemActive(false);
	}

	@Override
	public void delegateAIEnded(AIRobot ai) {
		if (ai instanceof AIRobotGotoBlock) {
			AIRobotGotoBlock aiGoto = (AIRobotGotoBlock) ai;

			if (((AIRobotGotoBlock) ai).unreachable) {
				robot.unreachableEntityDetected(target);
			}

			terminate();
		}
	}

	@Override
	public int getEnergyCost() {
		return 50;
	}
}
