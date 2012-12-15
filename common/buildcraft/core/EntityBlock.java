/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.core;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class EntityBlock extends Entity {

	public int texture = -1;
	public float shadowSize = 0;

	public float rotationX = 0;
	public float rotationY = 0;
	public float rotationZ = 0;

	public double iSize, jSize, kSize;

	public EntityBlock(World world) {
		super(world);
		preventEntitySpawning = false;
		noClip = true;
		isImmuneToFire = true;
	}

	public EntityBlock(World world, double xPos, double yPos, double zPos) {
		super(world);
		setPositionAndRotation(xPos, yPos, zPos, 0, 0);
	}

	public EntityBlock(World world, double i, double j, double k, double iSize, double jSize, double kSize) {
		this(world);
		this.iSize = iSize;
		this.jSize = jSize;
		this.kSize = kSize;
		setPositionAndRotation(i, j, k, 0, 0);
		this.motionX = 0.0;
		this.motionY = 0.0;
		this.motionZ = 0.0;
	}

	public EntityBlock(World world, double i, double j, double k, double iSize, double jSize, double kSize, int textureID) {
		this(world, i, j, k, iSize, jSize, kSize);
		texture = textureID;
	}

	@Override
	public void setPosition(double d, double d1, double d2) {
		super.setPosition(d, d1, d2);
		boundingBox.minX = posX;
		boundingBox.minY = posY;
		boundingBox.minZ = posZ;

		boundingBox.maxX = posX + iSize;
		boundingBox.maxY = posY + jSize;
		boundingBox.maxZ = posZ + kSize;
	}

	@Override
	public void moveEntity(double d, double d1, double d2) {
		setPosition(posX + d, posY + d1, posZ + d2);
	}

	@Override
	protected void entityInit() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound nbttagcompound) {
		iSize = nbttagcompound.getDouble("iSize");
		jSize = nbttagcompound.getDouble("jSize");
		kSize = nbttagcompound.getDouble("kSize");
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound nbttagcompound) {
		nbttagcompound.setDouble("iSize", iSize);
		nbttagcompound.setDouble("jSize", jSize);
		nbttagcompound.setDouble("kSize", kSize);
	}

}
