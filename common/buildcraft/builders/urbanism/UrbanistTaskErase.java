/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.builders.urbanism;

import buildcraft.core.robots.EntityRobot;

public class UrbanistTaskErase extends UrbanistTask {

	int x, y, z;
	boolean isDone = false;
	boolean inBreak = false;

	public UrbanistTaskErase (TileUrbanist urbanist, int x, int y, int z) {
		super (urbanist);

		this.x = x;
		this.y = y;
		this.z = z;
	}

	@Override
	public void setup(EntityRobot robot) {
		//robot.setDestinationAround(x, y, z);
	}

	@Override
	public void update(EntityRobot robot) {
		if (!inBreak && robot.getDistance(x, y, z) <= 10) {
			inBreak = true;
			robot.setLaserDestination(x + 0.5F, y + 0.5F, z + 0.5F);
			robot.showLaser();
		}

		if (inBreak) {
			//if (robot.breakBlock(x, y, z)) {
			//	isDone = true;
			//	robot.hideLaser();
			//}
		}
	}

	@Override
	public boolean done() {
		return isDone;
	}

}
