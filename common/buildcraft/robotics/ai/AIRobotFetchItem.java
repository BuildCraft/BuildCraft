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
import net.minecraft.entity.item.EntityItem;

import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.api.core.IZone;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.core.lib.inventory.TransactorSimple;
import buildcraft.core.lib.inventory.filters.IStackFilter;
import buildcraft.robotics.boards.BoardRobotPicker;

public class AIRobotFetchItem extends AIRobot {

	private EntityItem target;

	private float maxRange;
	private IStackFilter stackFilter;
	private int pickTime = -1;
	private IZone zone;

	public AIRobotFetchItem(EntityRobotBase iRobot) {
		super(iRobot);
	}

	public AIRobotFetchItem(EntityRobotBase iRobot, float iMaxRange, IStackFilter iStackFilter, IZone iZone) {
		this(iRobot);

		maxRange = iMaxRange;
		stackFilter = iStackFilter;
		zone = iZone;
	}

	@Override
	public void preempt(AIRobot ai) {
		if (target != null && target.isDead) {
			terminate();
		}
	}

	@Override
	public void update() {
		if (target == null) {
			scanForItem();
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
	public void delegateAIEnded(AIRobot ai) {
		if (ai instanceof AIRobotGotoBlock) {
			if (target == null) {
				// This would happen after a load. As we reached the item
				// location already, just consider that the item is not there
				// anymore and allow user to try to find another one.
				setSuccess(false);
				terminate();
				return;
			}

			if (!ai.success()) {
				robot.unreachableEntityDetected(target);
				setSuccess(false);
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

	private void scanForItem() {
		double previousDistance = Double.MAX_VALUE;
		TransactorSimple inventoryInsert = new TransactorSimple(robot);

		for (Object o : robot.worldObj.loadedEntityList) {
			Entity e = (Entity) o;

			if (!e.isDead
					&& e instanceof EntityItem
					&& !BoardRobotPicker.targettedItems.contains(e.getEntityId())
					&& !robot.isKnownUnreachable(e)
					&& (zone == null || zone.contains(e.posX, e.posY, e.posZ))) {
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
			if (Math.floor(target.posX) != Math.floor(robot.posX) || Math.floor(target.posY) != Math.floor(robot.posY)
					|| Math.floor(target.posZ) != Math.floor(robot.posZ)) {
				startDelegateAI(new AIRobotGotoBlock(robot, (int) Math.floor(target.posX),
						(int) Math.floor(target.posY), (int) Math.floor(target.posZ)));
			}
		} else {
			// No item was found, terminate this AI
			setSuccess(false);
			terminate();
		}
	}

	@Override
	public int getEnergyCost() {
		return 15;
	}
}
