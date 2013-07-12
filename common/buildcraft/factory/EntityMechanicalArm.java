/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.factory;

import buildcraft.core.EntityBlock;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class EntityMechanicalArm extends Entity {
	EntityBlock xArm, yArm, zArm, head;

	boolean inProgressionXZ = false;
	boolean inProgressionY = false;

	protected TileQuarry parent;

	private double armSizeX;
	private double armSizeZ;
	private double xRoot;
	private double yRoot;
	private double zRoot;

	private int headX, headY, headZ;

	public EntityMechanicalArm(World world) {
		super(world);
		makeParts(world);
	}

	public EntityMechanicalArm(World world, double i, double j, double k, double width, double height, TileQuarry parent) {
		this(world);
		setPositionAndRotation(parent.xCoord, parent.yCoord, parent.zCoord, 0, 0);
		this.xRoot = i;
		this.yRoot = j;
		this.zRoot = k;
		this.motionX = 0.0;
		this.motionY = 0.0;
		this.motionZ = 0.0;
		setArmSize(width, height);
		setHead(i, j - 2, k);

		noClip = true;

		this.parent = parent;
		parent.setArm(this);
		updatePosition();
	}

	void setHead(double x, double y, double z) {
		this.headX = (int) (x * 32D);
		this.headY = (int) (y * 32D);
		this.headZ = (int) (z * 32D);
	}

	private void setArmSize(double x, double z) {
		armSizeX = x;
		xArm.iSize = x;
		armSizeZ = z;
		zArm.kSize = z;
		updatePosition();
	}

	private void makeParts(World world) {
		xArm = FactoryProxy.proxy.newDrill(world, 0, 0, 0, 1, 0.5, 0.5);
		yArm = FactoryProxy.proxy.newDrill(world, 0, 0, 0, 0.5, 1, 0.5);
		zArm = FactoryProxy.proxy.newDrill(world, 0, 0, 0, 0.5, 0.5, 1);

		head = FactoryProxy.proxy.newDrillHead(world, 0, 0, 0, 0.2, 1, 0.2);
		head.shadowSize = 1.0F;

		world.spawnEntityInWorld(xArm);
		world.spawnEntityInWorld(yArm);
		world.spawnEntityInWorld(zArm);
		world.spawnEntityInWorld(head);
	}

	@Override
	protected void entityInit() {
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound nbttagcompound) {
		xRoot = nbttagcompound.getDouble("xRoot");
		yRoot = nbttagcompound.getDouble("yRoot");
		zRoot = nbttagcompound.getDouble("zRoot");
		armSizeX = nbttagcompound.getDouble("armSizeX");
		armSizeZ = nbttagcompound.getDouble("armSizeZ");
		setArmSize(armSizeX, armSizeZ);
		updatePosition();
	}

	private void findAndJoinQuarry() {
		TileEntity te = worldObj.getBlockTileEntity((int) posX, (int) posY, (int) posZ);
		if (te != null && te instanceof TileQuarry) {
			parent = (TileQuarry) te;
			parent.setArm(this);
		} else {
			setDead();
		}
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound nbttagcompound) {
		nbttagcompound.setDouble("xRoot", xRoot);
		nbttagcompound.setDouble("yRoot", yRoot);
		nbttagcompound.setDouble("zRoot", zRoot);
		nbttagcompound.setDouble("armSizeX", armSizeX);
		nbttagcompound.setDouble("armSizeZ", armSizeZ);
	}

	@Override
	public void onUpdate() {
		super.onUpdate();
		updatePosition();
		if (parent == null) {
			findAndJoinQuarry();
		}

		if (parent == null) {
			setDead();
			return;
		}
	}

	public void updatePosition() {
		double[] head = getHead();
		this.xArm.setPosition(xRoot, yRoot, head[2] + 0.25);
		this.yArm.jSize = yRoot - head[1] - 1;
		this.yArm.setPosition(head[0] + 0.25, head[1] + 1, head[2] + 0.25);
		this.zArm.setPosition(head[0] + 0.25, yRoot, zRoot);
		this.head.setPosition(head[0] + 0.4, head[1], head[2] + 0.4);
	}

	@Override
	public void setDead() {
		if (worldObj != null && worldObj.isRemote) {
			xArm.setDead();
			yArm.setDead();
			zArm.setDead();
			head.setDead();
		}
		super.setDead();
	}

	private double[] getHead() {
		return new double[] { this.headX / 32D, this.headY / 32D, this.headZ / 32D };
	}
}
