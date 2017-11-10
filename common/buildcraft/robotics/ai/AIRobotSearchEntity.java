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

import buildcraft.api.core.IZone;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.core.lib.utils.IEntityFilter;

public class AIRobotSearchEntity extends AIRobot {

	public Entity target;

	private float maxRange;
	private IZone zone;
	private IEntityFilter filter;

	public AIRobotSearchEntity(EntityRobotBase iRobot) {
		super(iRobot);
	}

	public AIRobotSearchEntity(EntityRobotBase iRobot, IEntityFilter iFilter, float iMaxRange, IZone iZone) {
		this(iRobot);

		maxRange = iMaxRange;
		zone = iZone;
		filter = iFilter;
	}

	@Override
	public void start() {
		double previousDistance = Double.MAX_VALUE;

		for (Object o : robot.worldObj.loadedEntityList) {
			Entity e = (Entity) o;

			if (!e.isDead
					&& filter.matches(e)
					&& (zone == null || zone.contains(e.posX, e.posY, e.posZ))
					&& (!robot.isKnownUnreachable(e))) {
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
						target = e;
					} else {
						if (sqrDistance < previousDistance) {
							previousDistance = sqrDistance;
							target = e;
						}
					}
				}
			}
		}

		terminate();
	}

	@Override
	public boolean success() {
		return target != null;
	}

	@Override
	public int getEnergyCost() {
		return 2;
	}
}
