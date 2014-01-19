/**
 * Copyright (c) 2011 - 2014 SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License
 * 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.core;

import buildcraft.core.proxy.CoreProxy;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import cpw.mods.fml.common.registry.IEntityAdditionalSpawnData;

public class EntityRobot extends EntityLivingBase implements IEntityAdditionalSpawnData {

	protected int aroundX, aroundY, aroundZ;

	protected float destX, destY, destZ;

	double dirX, dirY, dirZ;

	public LaserData laser = new LaserData ();

	private boolean needsUpdate = false;

	public EntityRobot(World par1World) {
		super(par1World);

		dirX = 0;
		dirY = 0;
		dirZ = 0;

		motionX = 0;
		motionY = 0;
		motionZ = 0;

		ignoreFrustumCheck = true;
	}

	@Override
	protected void entityInit() {
		super.entityInit();

		preventEntitySpawning = false;
		noClip = true;
		isImmuneToFire = true;

		dataWatcher.addObject(10, Integer.valueOf(0));
		dataWatcher.addObject(11, Integer.valueOf(0));
		dataWatcher.addObject(12, Integer.valueOf(0));
		dataWatcher.addObject(13, Byte.valueOf((byte) 0));
	}

	protected int encodeDouble(double d) {
		return (int) (d * 8192);
	}

	protected double decodeDouble(int i) {
		return (i / 8192D);
	}

	protected void updateDataClient() {
		laser.tail.x = decodeDouble(dataWatcher.getWatchableObjectInt(10));
		laser.tail.y = decodeDouble(dataWatcher.getWatchableObjectInt(11));
		laser.tail.z = decodeDouble(dataWatcher.getWatchableObjectInt(12));
		laser.isVisible = (dataWatcher.getWatchableObjectByte(13) == 1);
	}

	protected void updateDataServer() {
		dataWatcher.updateObject(10, Integer.valueOf(encodeDouble(laser.tail.x)));
		dataWatcher.updateObject(11, Integer.valueOf(encodeDouble(laser.tail.y)));
		dataWatcher.updateObject(12, Integer.valueOf(encodeDouble(laser.tail.z)));
		dataWatcher.updateObject(13, Byte.valueOf((byte) (laser.isVisible ? 1 : 0)));
	}

	protected void init() {

	}

	public void setLaserDestination (float x, float y, float z) {
		laser.tail.x = x;
		laser.tail.y = y;
		laser.tail.z = z;

		needsUpdate = true;
	}

	public void showLaser () {
		if (laser != null) {
			laser.isVisible = true;
			needsUpdate = true;
		}
	}

	public void hideLaser () {
		if (laser != null) {
			laser.isVisible = false;
			needsUpdate = true;
		}
	}

	public void setDestination(float x, float y, float z) {
		destX = x;
		destY = y;
		destZ = z;

		dirX = (destX - posX);
		dirY = (destY - posY);
		dirZ = (destZ - posZ);

		double magnitude = Math.sqrt(dirX * dirX + dirY * dirY + dirZ * dirZ);

		dirX /= magnitude;
		dirY /= magnitude;
		dirZ /= magnitude;
	}

	public void setDestinationAround(int x, int y, int z) {
		aroundX = x;
		aroundY = y;
		aroundZ = z;

		randomDestination();
	}

	public void randomDestination() {
		for (int i = 0; i < 3; ++i) {
			float testX = aroundX + rand.nextFloat() * 10F - 5F;
			float testY = aroundY + rand.nextFloat() * 5F;
			float testZ = aroundZ + rand.nextFloat() * 10F - 5F;

			int blockId = worldObj.getBlockId((int) testX, (int) testY,
					(int) testZ);

			if (Block.blocksList[blockId] == null
					|| Block.blocksList[blockId].isAirBlock(worldObj,
							(int) testX, (int) testY, (int) testZ)) {
				setDestination(testX, testY, testZ);

				return;
			}
		}
	}

	double prevDistance = Double.MAX_VALUE;

	@Override
	public void onUpdate() {
		if (CoreProxy.proxy.isSimulating(worldObj) && needsUpdate) {
			updateDataServer();
			needsUpdate = false;
		}

		if (CoreProxy.proxy.isRenderWorld(worldObj)) {
			updateDataClient();
		}

		if (CoreProxy.proxy.isSimulating(worldObj)) {
			double distance = getDistance(destX, destY, destZ);

			if (distance >= prevDistance) {
				randomDestination();
			}

			prevDistance = getDistance(destX, destY, destZ);

			motionX = dirX / 10F;
			motionY = dirY / 10F;
			motionZ = dirZ / 10F;
		}

		if (laser.isVisible) {
			boundingBox.minX = Math.min(posX, laser.tail.x);
			boundingBox.minY = Math.min(posY, laser.tail.y);
			boundingBox.minZ = Math.min(posZ, laser.tail.z);

			boundingBox.maxX = Math.max(posX, laser.tail.x);
			boundingBox.maxY = Math.max(posY, laser.tail.y);
			boundingBox.maxZ = Math.max(posZ, laser.tail.z);

			boundingBox.minX--;
			boundingBox.minY--;
			boundingBox.minZ--;

			boundingBox.maxX++;
			boundingBox.maxY++;
			boundingBox.maxZ++;
		} else {
			boundingBox.minX = posX - 1;
			boundingBox.minY = posY - 1;
			boundingBox.minZ = posZ - 1;

			boundingBox.maxX = posX + 1;
			boundingBox.maxY = posY + 1;
			boundingBox.maxZ = posZ + 1;
		}

		super.onUpdate();
	}

	protected void move() {

	}

	protected boolean reachedDesination() {
		return getDistance(destX, destY, destZ) <= 0.2F;
	}


	@Override
	public void writeSpawnData(ByteArrayDataOutput data) {

	}

	@Override
	public void readSpawnData(ByteArrayDataInput data) {
		init();
	}

	@Override
	public ItemStack getHeldItem() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ItemStack getCurrentItemOrArmor(int i) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setCurrentItemOrArmor(int i, ItemStack itemstack) {
		// TODO Auto-generated method stub

	}

	@Override
	public ItemStack[] getLastActiveItems() {
		return new ItemStack [0];
	}


	@Override
    protected void fall(float par1) {}

	@Override
    protected void updateFallState(double par1, boolean par3) {}

	@Override
	public void moveEntityWithHeading(float par1, float par2) {
		this.setPosition(posX + motionX, posY + motionY, posZ + motionZ);
	}

	@Override
    public boolean isOnLadder() {
        return false;
    }

}
