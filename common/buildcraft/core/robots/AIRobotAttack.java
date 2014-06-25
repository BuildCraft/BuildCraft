/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.robots;

import net.minecraft.entity.monster.EntityMob;

import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;

public class AIRobotAttack extends AIRobot {

	public EntityMob target;

	private int delay = 10;

	public AIRobotAttack(EntityRobotBase iRobot, EntityMob iTarget) {
		super(iRobot, 5, 1);

		target = iTarget;
	}

	@Override
	public void start() {
		robot.setItemActive(true);
		robot.aimItemAt((int) Math.floor(target.posX), (int) Math.floor(target.posY),
				(int) Math.floor(target.posZ));
	}

	@Override
	public void update() {
		if (target.isDead) {
			terminate();
			return;
		}

		if (robot.getDistanceToEntity(target) > 2.0) {
			terminate();
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
}
