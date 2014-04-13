/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.robots;

import net.minecraft.nbt.NBTTagCompound;


public class AIReturnToDock extends AIBase {

	double prevDistance = Double.MAX_VALUE;

	int phase = 0;

	@Override
	public void update(EntityRobot robot) {
		if (robot.worldObj.isRemote) {
			return;
		}

		if (phase == 0) {
			float x = robot.dockingStation.x + 0.5F + robot.dockingStation.side.offsetX * 1.5F;
			float y = robot.dockingStation.y + 0.5F +  robot.dockingStation.side.offsetY * 1.5F;
			float z = robot.dockingStation.z + 0.5F +  robot.dockingStation.side.offsetZ * 1.5F;

			setDestination(robot, x, y, z);
			phase = 1;
		} else if (phase == 1) {
			double distance = robot.getDistance(destX, destY, destZ);

			if (distance >= prevDistance) {
				prevDistance = Double.MAX_VALUE;
				float x = robot.dockingStation.x + 0.5F +  robot.dockingStation.side.offsetX * 0.5F;
				float y = robot.dockingStation.y + 0.5F +  robot.dockingStation.side.offsetY * 0.5F;
				float z = robot.dockingStation.z + 0.5F +  robot.dockingStation.side.offsetZ * 0.5F;
				setDestination(robot, x, y, z);

				phase = 2;
			} else {
				prevDistance = distance;
			}
		} else if (phase == 2) {
			double distance = robot.getDistance(destX, destY, destZ);

			if (distance >= prevDistance) {
				float x = robot.dockingStation.x + 0.5F +  robot.dockingStation.side.offsetX * 0.5F;
				float y = robot.dockingStation.y + 0.5F +  robot.dockingStation.side.offsetY * 0.5F;
				float z = robot.dockingStation.z + 0.5F +  robot.dockingStation.side.offsetZ * 0.5F;

				robot.motionX = 0;
				robot.motionY = 0;
				robot.motionZ = 0;

				robot.setPosition(x, y, z);
				robot.currentAI = new AIDocked();
			} else {
				prevDistance = distance;
			}
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);

		nbt.setInteger("phase", phase);
    }

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);

		phase = nbt.getInteger("phase");
	}
}
