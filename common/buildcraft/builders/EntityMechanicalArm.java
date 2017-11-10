/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.builders;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import buildcraft.core.lib.EntityBlock;

public class EntityMechanicalArm extends Entity {

	protected TileQuarry parent;

	private double armSizeX;
	private double armSizeZ;
	private double xRoot;
	private double yRoot;
	private double zRoot;

	private int headX, headY, headZ;
	private EntityBlock xArm, yArm, zArm, head;

	public EntityMechanicalArm(World world) {
		super(world);
		makeParts(world);
		noClip = true;
	}

	public EntityMechanicalArm(World world, double x, double y, double z, double width, double height, TileQuarry parent) {
		this(world);
		setPositionAndRotation(parent.xCoord, parent.yCoord, parent.zCoord, 0, 0);
		this.xRoot = x;
		this.yRoot = y;
		this.zRoot = z;
		this.motionX = 0.0;
		this.motionY = 0.0;
		this.motionZ = 0.0;
		setArmSize(width, height);
		setHead(x, y - 2, z);
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
		xArm = BuilderProxy.proxy.newDrill(world, 0, 0, 0, 1, 0.5, 0.5, true);
		yArm = BuilderProxy.proxy.newDrill(world, 0, 0, 0, 0.5, 1, 0.5, false);
		zArm = BuilderProxy.proxy.newDrill(world, 0, 0, 0, 0.5, 0.5, 1, true);

		head = BuilderProxy.proxy.newDrillHead(world, 0, 0, 0, 0.2, 1, 0.2);
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
		TileEntity te = worldObj.getTileEntity((int) posX, (int) posY, (int) posZ);
		if (te instanceof TileQuarry) {
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
		double[] headT = getHead();
		this.xArm.setPosition(xRoot, yRoot, headT[2] + 0.25);
		this.yArm.jSize = yRoot - headT[1] - 1;
		this.yArm.setPosition(headT[0] + 0.25, headT[1] + 1, headT[2] + 0.25);
		this.zArm.setPosition(headT[0] + 0.25, yRoot, zRoot);
		this.head.setPosition(headT[0] + 0.4, headT[1], headT[2] + 0.4);
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
		return new double[]{this.headX / 32D, this.headY / 32D, this.headZ / 32D};
	}
}
