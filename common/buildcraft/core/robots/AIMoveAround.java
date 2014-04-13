/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.robots;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;

public class AIMoveAround extends AIBase {

	protected float aroundX, aroundY, aroundZ;

	double prevDistance = Double.MAX_VALUE;

	public AIMoveAround () {

	}

	public AIMoveAround (EntityRobot robot, float x, float y, float z) {
		aroundX = x;
		aroundY = y;
		aroundZ = z;

		randomDestination(robot);
	}

	@Override
	public void update(EntityRobot robot) {
		if (robot.worldObj.isRemote) {
			return;
		}

		double distance = robot.getDistance(destX, destY, destZ);

		if (distance >= prevDistance) {
			randomDestination(robot);
			prevDistance = Double.MAX_VALUE;
		} else {
			prevDistance = robot.getDistance(destX, destY, destZ);
		}
	}

	public void randomDestination(EntityRobot robot) {
		for (int i = 0; i < 5; ++i) {
			float testX = aroundX + robot.worldObj.rand.nextFloat() * 10F - 5F;
			float testY = aroundY + robot.worldObj.rand.nextFloat() * 5F;
			float testZ = aroundZ + robot.worldObj.rand.nextFloat() * 10F - 5F;

			Block block = robot.worldObj.getBlock((int) testX, (int) testY,
					(int) testZ);

			// We set a destination. If it's wrong, we try a new one.
			// Eventually, we'll accept even a wrong one if none can be easily
			// found.

			setDestination(robot, testX, testY, testZ);

			if (block == Blocks.air) {
				return;
			}
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);

		nbt.setFloat("aroundX", aroundX);
		nbt.setFloat("aroundY", aroundY);
		nbt.setFloat("aroundZ", aroundZ);
    }

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);

		aroundX = nbt.getFloat("aroundX");
		aroundY = nbt.getFloat("aroundY");
		aroundZ = nbt.getFloat("aroundZ");
	}
}
