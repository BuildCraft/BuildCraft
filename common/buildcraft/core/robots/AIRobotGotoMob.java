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
import net.minecraft.entity.monster.EntityMob;

import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.core.inventory.TransactorSimple;

public class AIRobotGotoMob extends AIRobot {

	public EntityMob target;

	private float maxRange;

	public AIRobotGotoMob(EntityRobotBase iRobot, float iMaxRange) {
		super(iRobot, 0, 1);

		maxRange = iMaxRange;
	}

	@Override
	public void start() {
		double previousDistance = Double.MAX_VALUE;
		TransactorSimple inventoryInsert = new TransactorSimple(robot);

		for (Object o : robot.worldObj.loadedEntityList) {
			Entity e = (Entity) o;

			if (!e.isDead && e instanceof EntityMob) {
				double dx = e.posX - robot.posX;
				double dy = e.posY - robot.posY;
				double dz = e.posZ - robot.posZ;

				double sqrDistance = dx * dx + dy * dy + dz * dz;
				double maxDistance = maxRange * maxRange;

				if (sqrDistance >= maxDistance) {
					continue;
				} else {
					if (target == null) {
						previousDistance = sqrDistance;
						target = (EntityMob) e;
					} else {
						if (sqrDistance < previousDistance) {
							previousDistance = sqrDistance;
							target = (EntityMob) e;
						}
					}
				}
			}
		}

		if (target != null) {
			startDelegateAI(new AIRobotGotoBlock(robot, (int) Math.floor(target.posX),
					(int) Math.floor(target.posY), (int) Math.floor(target.posZ)));

		} else {
			// No mob was found, terminate this AI
			terminate();
		}
	}

	@Override
	public void preempt(AIRobot ai) {
		if (target.isDead) {
			terminate();
		}
	}

	@Override
	public void update() {
		if (target.isDead) {
			terminate();
		} else {
			// fight
			terminate();
		}
	}
}
