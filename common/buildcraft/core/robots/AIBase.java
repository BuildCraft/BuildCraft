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

public abstract class AIBase {

	protected float destX, destY, destZ;
	protected double dirX, dirY, dirZ;

	public abstract void update (EntityRobot robot);

	public void setDestination(EntityRobot robot, float x, float y, float z) {
		destX = x;
		destY = y;
		destZ = z;

		dirX = destX - robot.posX;
		dirY = destY - robot.posY;
		dirZ = destZ - robot.posZ;

		double magnitude = Math.sqrt(dirX * dirX + dirY * dirY + dirZ * dirZ);

		dirX /= magnitude;
		dirY /= magnitude;
		dirZ /= magnitude;

		robot.motionX = dirX / 10F;
		robot.motionY = dirY / 10F;
		robot.motionZ = dirZ / 10F;
	}

	public void writeToNBT(NBTTagCompound nbt) {
		nbt.setFloat("destX", destX);
		nbt.setFloat("destY", destY);
		nbt.setFloat("destZ", destZ);

		nbt.setDouble("dirX", dirX);
		nbt.setDouble("dirY", dirY);
		nbt.setDouble("dirZ", dirZ);
    }

	public void readFromNBT(NBTTagCompound nbt) {
		destX = nbt.getFloat("destX");
		destY = nbt.getFloat("destY");
		destZ = nbt.getFloat("destZ");

		dirX = nbt.getDouble("dirX");
		dirY = nbt.getDouble("dirY");
		dirZ = nbt.getDouble("dirZ");
	}
}
