/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.robots;

import buildcraft.api.core.BlockIndex;
import buildcraft.api.core.IBox;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.core.Box;

public class AIRobotFindRandomGroundBlock extends AIRobot {

	private static final int MAX_ATTEMPTS = 4096;

	public BlockIndex blockFound;

	private int range;
	private IBlockFilter filter;
	private IBox area;
	private int attempts = 0;

	public AIRobotFindRandomGroundBlock(EntityRobotBase iRobot, int iRange, IBlockFilter iFilter, IBox iArea) {
		super(iRobot, 2, 1);

		range = iRange;
		filter = iFilter;
		area = iArea;
	}

	@Override
	public void update() {
		attempts++;

		if (attempts > MAX_ATTEMPTS) {
			terminate();
		}

		int x = 0;
		int z = 0;

		if (area == null) {
			double r = robot.worldObj.rand.nextFloat() * range;
			double a = robot.worldObj.rand.nextFloat() * 2.0 * Math.PI;

			x = (int) (Math.cos(a) * r + Math.floor(robot.posX));
			z = (int) (Math.sin(a) * r + Math.floor(robot.posZ));
		} else {
			x = (int) area.pMin().x + robot.worldObj.rand.nextInt(((Box) area).sizeX());
			z = (int) area.pMin().z + robot.worldObj.rand.nextInt(((Box) area).sizeZ());
		}

		for (int y = robot.worldObj.getHeight(); y >= 0; --y) {
			if (filter.matches(robot.worldObj, x, y, z)) {
				blockFound = new BlockIndex(x, y, z);
				terminate();
				return;
			} else if (!robot.worldObj.isAirBlock(x, y, z)) {
				return;
			}
		}
	}
}
