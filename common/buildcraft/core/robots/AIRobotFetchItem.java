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
import net.minecraft.entity.item.EntityItem;

import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.core.inventory.TransactorSimple;
import buildcraft.core.inventory.filters.IStackFilter;
import buildcraft.core.robots.boards.BoardRobotPicker;

public class AIRobotFetchItem extends AIRobot {

	public EntityItem target;

	private float maxRange;
	private IStackFilter stackFilter;
	private int pickTime = -1;

	public AIRobotFetchItem(EntityRobotBase iRobot, float iMaxRange, IStackFilter iStackFilter) {
		super(iRobot, 0, 1);

		maxRange = iMaxRange;
		stackFilter = iStackFilter;
	}

	@Override
	public void start() {
		double previousDistance = Double.MAX_VALUE;
		TransactorSimple inventoryInsert = new TransactorSimple(robot);

		for (Object o : robot.worldObj.loadedEntityList) {
			Entity e = (Entity) o;

			if (!e.isDead && e instanceof EntityItem && !BoardRobotPicker.targettedItems.contains(e.getEntityId())) {
				double dx = e.posX - robot.posX;
				double dy = e.posY - robot.posY;
				double dz = e.posZ - robot.posZ;

				double sqrDistance = dx * dx + dy * dy + dz * dz;
				double maxDistance = maxRange * maxRange;

				if (sqrDistance >= maxDistance) {
					continue;
				} else if (stackFilter != null && !stackFilter.matches(((EntityItem) e).getEntityItem())) {
					continue;
				} else {
					EntityItem item = (EntityItem) e;

					if (inventoryInsert.inject(item.getEntityItem(), ForgeDirection.UNKNOWN, false) > 0) {
						if (target == null) {
							previousDistance = sqrDistance;
							target = item;
						} else {
							if (sqrDistance < previousDistance) {
								previousDistance = sqrDistance;
								target = item;
							}
						}
					}
				}
			}
		}

		if (target != null) {
			BoardRobotPicker.targettedItems.add(target.getEntityId());

			startDelegateAI(new AIRobotGotoBlock(robot, (int) Math.floor(target.posX),
					(int) Math.floor(target.posY), (int) Math.floor(target.posZ)));

		} else {
			// No item was found, terminate this AI
			terminate();
		}
	}

	@Override
	public void preempt(AIRobot ai) {
		if (target.isDead) {
			BoardRobotPicker.targettedItems.remove(target.getEntityId());
			terminate();
		}
	}

	@Override
	public void update() {
		if (target.isDead) {
			terminate();
		} else {
			pickTime++;

			if (pickTime > 5) {
				TransactorSimple inventoryInsert = new TransactorSimple(robot);

				target.getEntityItem().stackSize -= inventoryInsert.inject(
						target.getEntityItem(), ForgeDirection.UNKNOWN,
						true);

				if (target.getEntityItem().stackSize <= 0) {
					target.setDead();
				}

				terminate();
			}
		}
	}

	@Override
	public void end() {
		if (target != null) {
			BoardRobotPicker.targettedItems.remove(target.getEntityId());
		}
	}
}
