/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.robots;

import net.minecraft.util.BlockPos;
import buildcraft.api.core.IZone;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;

public class AIRobotSearchRandomGroundBlock extends AIRobot {

	private static final int MAX_ATTEMPTS = 4096;

	public BlockPos blockFound;

	private int range;
	private IBlockFilter filter;
	private IZone zone;
	private int attempts = 0;

	public AIRobotSearchRandomGroundBlock(EntityRobotBase iRobot) {
		super(iRobot);
	}

	public AIRobotSearchRandomGroundBlock(EntityRobotBase iRobot, int iRange, IBlockFilter iFilter, IZone iZone) {
		super(iRobot);

		range = iRange;
		filter = iFilter;
		zone = iZone;
	}

	@Override
	public void update() {
		if (filter == null) {
			// defensive code
			terminate();
		}

		attempts++;

		if (attempts > MAX_ATTEMPTS) {
			terminate();
		}

		int x = 0;
		int z = 0;

		if (zone == null) {
			double r = robot.worldObj.rand.nextFloat() * range;
			double a = robot.worldObj.rand.nextFloat() * 2.0 * Math.PI;

			x = (int) (Math.cos(a) * r + Math.floor(robot.posX));
			z = (int) (Math.sin(a) * r + Math.floor(robot.posZ));
		} else {
			BlockPos b = zone.getRandomBlockPos(robot.worldObj.rand);
			x = b.getX();
			z = b.getZ();
		}

		for (int y = robot.worldObj.getHeight(); y >= 0; --y) {
			BlockPos pos = new BlockPos(x, y, z);
			if (filter.matches(robot.worldObj, pos)) {
				blockFound = pos;
				terminate();
				return;
			} else if (!robot.worldObj.isAirBlock(pos)) {
				return;
			}
		}
	}
}
