/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.factory;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

import cpw.mods.fml.common.registry.IEntityAdditionalSpawnData;
import buildcraft.BuildCraftFactory;
import buildcraft.core.EntityBlock;
import net.minecraft.src.Entity;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;

public class EntityMechanicalArm extends Entity implements IEntityAdditionalSpawnData {
	EntityBlock xArm, yArm, zArm, head;

	public IArmListener listener;
	boolean inProgressionXZ = false;
	boolean inProgressionY = false;

	protected TileQuarry parent;

	private double armSizeX;
	private double armSizeZ;

	private double xRoot;

	private double yRoot;

	private double zRoot;

	private double angle;

	public EntityMechanicalArm(World world) {
		super(world);
		makeParts(world);
		// Head X, Y, Z
		dataWatcher.addObject(2, 1);
		dataWatcher.addObject(3, 1);
		dataWatcher.addObject(4, 1);

		// Target X, Y, Z
		dataWatcher.addObject(5, 1);
		dataWatcher.addObject(6, 1);
		dataWatcher.addObject(7, 1);

		// Speed
		dataWatcher.addObject(8, (int)(0.03 * 8000D));
	}

	public EntityMechanicalArm(World world, double i, double j, double k, double width, double height, TileQuarry parent) {
		this(world);
		setPositionAndRotation(parent.xCoord, parent.yCoord, parent.zCoord, 0, 0);
		this.xRoot = i;
		this.yRoot = j;
		this.zRoot = k;
		setVelocity(0, 0, 0);
		setArmSize(width, height);
		setHead(i, j - 2, k);
		setTarget(i, j - 2, k);

		noClip = true;
		inProgressionXZ = false;
		inProgressionY = false;

		this.parent = parent;
		parent.setArm(this);
		updatePosition();
	}

	public void setArmSpeed(double speed)
	{
		dataWatcher.updateObject(8, (int)(speed * 8000D));
	}

	public double getArmSpeed()
	{
		return dataWatcher.getWatchableObjectInt(8) / 8000D;
	}

	void setHead(double x, double y, double z)
	{
		dataWatcher.updateObject(2, ((int)(x * 32D)));
		dataWatcher.updateObject(3, ((int)(y * 32D)));
		dataWatcher.updateObject(4, ((int)(z * 32D)));
	}

	void setTarget(double x, double y, double z) {
		dataWatcher.updateObject(5, ((int)(x * 32D)));
		dataWatcher.updateObject(6, ((int)(y * 32D)));
		dataWatcher.updateObject(7, ((int)(z * 32D)));
	}

	private void setArmSize(double x, double z)
	{
		armSizeX = x;
		xArm.iSize = x;
		armSizeZ = z;
		zArm.kSize = z;
		updatePosition();
	}

	private void makeParts(World world) {
		xArm = new EntityBlock(world, 0, 0, 0, 1, 0.5, 0.5);
		xArm.texture = BuildCraftFactory.drillTexture;

		yArm = new EntityBlock(world, 0, 0, 0, 0.5, 1, 0.5);
		yArm.texture = BuildCraftFactory.drillTexture;

		zArm = new EntityBlock(world, 0, 0, 0, 0.5, 0.5, 1);
		zArm.texture = BuildCraftFactory.drillTexture;

		head = new EntityBlock(world, 0, 0, 0, 0.2, 1, 0.2);
		head.texture = 2 * 16 + 10;
		head.shadowSize = 1.0F;

		world.spawnEntityInWorld(xArm);
		world.spawnEntityInWorld(yArm);
		world.spawnEntityInWorld(zArm);
		world.spawnEntityInWorld(head);
	}

	@Override
	protected void entityInit() {}

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
		TileEntity te = worldObj.getBlockTileEntity((int)posX, (int)posY, (int)posZ);
		if (te != null && te instanceof TileQuarry)
		{
			parent = (TileQuarry) te;
			parent.setArm(this);

		}
		else
		{
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
		if (worldObj.isRemote)
		{
			super.onUpdate();
			updatePosition();
			return;
		}
		super.onUpdate();
		if (parent == null)
		{
			findAndJoinQuarry();
		}

		if (parent == null)
		{
			setDead();
			return;
		}

		double[] target = getTarget();
		double[] head = getHead();

		double dX = target[0] - head[0];
		double dY = target[1] - head[1];
		double dZ = target[2] - head[2];

		if (dX != 0 || dY != 0 || dZ != 0)
		{
			angle = Math.atan2(target[2]-head[2], target[0]-head[0]);
			inProgressionXZ = true;
			inProgressionY = true;
		}

		if (getArmSpeed() > 0) {
			doMove(getArmSpeed());
		}
	}

	public void doMove(double instantSpeed) {
		double[] target = getTarget();
		double[] head = getHead();

		if (inProgressionXZ) {
			if (Math.abs(target[0] - head[0]) < instantSpeed * 2 && Math.abs(target[2] - head[2]) < instantSpeed * 2) {
				head[0] = target[0];
				head[2] = target[2];

				inProgressionXZ = false;

				if (listener != null && !inProgressionY) {
					listener.positionReached(this);
					head[1] = target[1];
				}
			} else {
				head[0] += Math.cos(angle) * instantSpeed;
				head[2] += Math.sin(angle) * instantSpeed;
			}
			setHead(head[0], head[1], head[2]);
		}

		if (inProgressionY) {
			if (Math.abs(target[1] - head[1]) < instantSpeed * 2) {
				head[1] = target[1];

				inProgressionY = false;

				if (listener != null && !inProgressionXZ) {
					listener.positionReached(this);
					head[0] = target[0];
					head[2] = target[2];
				}
			} else {
				if (target[1] > head[1]) {
					head[1] += instantSpeed / 2;
				} else {
					head[1] -= instantSpeed / 2;
				}
			}
			setHead(head[0],head[1],head[2]);
		}
		updatePosition();
	}

	public void updatePosition() {
		double[] head = getHead();
		this.xArm.setPosition(xRoot, yRoot, head[2] + 0.25);
		this.yArm.jSize = yRoot - head[1]- 1;
		this.yArm.setPosition(head[0] + 0.25, head[1] + 1, head[2] + 0.25);
		this.zArm.setPosition(head[0] + 0.25, yRoot, zRoot);
		this.head.setPosition(head[0] + 0.4, head[1], head[2] + 0.4);
	}


	public void joinToWorld(World w) {
		if (!w.isRemote)
		{
			w.spawnEntityInWorld(this);
		}
	}

	@Override
	public void setDead() {
		if (worldObj!=null && worldObj.isRemote)
		{
			xArm.setDead();
			yArm.setDead();
			zArm.setDead();
			head.setDead();
		}
		super.setDead();
	}

	@Override
	public void writeSpawnData(ByteArrayDataOutput data) {
		data.writeDouble(armSizeX);
		data.writeDouble(armSizeZ);
	}

	@Override
	public void readSpawnData(ByteArrayDataInput data) {
		armSizeX = data.readDouble();
		armSizeZ = data.readDouble();
		setArmSize(armSizeX, armSizeZ);
		updatePosition();
	}

	public double[] getTarget()
	{
		return new double[] { this.dataWatcher.getWatchableObjectInt(5) / 32D, this.dataWatcher.getWatchableObjectInt(6) / 32D, this.dataWatcher.getWatchableObjectInt(7) / 32D };
	}

	public double[] getHead()
	{
		return new double[] { this.dataWatcher.getWatchableObjectInt(2) / 32D, this.dataWatcher.getWatchableObjectInt(3) / 32D, this.dataWatcher.getWatchableObjectInt(4) / 32D };
	}
}
