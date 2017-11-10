/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.robotics.ai;

import net.minecraft.entity.Entity;

import buildcraft.api.blueprints.BuilderAPI;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.robotics.EntityRobot;

public class AIRobotAttack extends AIRobot {

	private Entity target;

	private int delay = 10;

	public AIRobotAttack(EntityRobotBase iRobot) {
		super(iRobot);
	}

	public AIRobotAttack(EntityRobotBase iRobot, Entity iTarget) {
		this(iRobot);

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
		if (target == null || target.isDead) {
			terminate();
			return;
		}

		if (robot.getDistanceToEntity(target) > 2.0) {
			startDelegateAI(new AIRobotGotoBlock(robot, (int) Math.floor(target.posX),
					(int) Math.floor(target.posY), (int) Math.floor(target.posZ)));
			robot.setItemActive(false);

			return;
		}

		delay++;

		if (delay > 20) {
			delay = 0;
			((EntityRobot) robot).attackTargetEntityWithCurrentItem(target);
			robot.aimItemAt((int) Math.floor(target.posX), (int) Math.floor(target.posY),
					(int) Math.floor(target.posZ));
		}
	}

	@Override
	public void end() {
		robot.setItemActive(false);
	}

	@Override
	public void delegateAIEnded(AIRobot ai) {
		if (ai instanceof AIRobotGotoBlock) {
			if (!ai.success()) {
				robot.unreachableEntityDetected(target);
			}
			terminate();
		}
	}

	@Override
	public int getEnergyCost() {
		return BuilderAPI.BREAK_ENERGY * 2 / 20;
	}
}
