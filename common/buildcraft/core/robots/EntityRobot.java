/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.robots;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import buildcraft.core.DefaultProps;
import buildcraft.core.LaserData;
import buildcraft.transport.TileGenericPipe;
import cpw.mods.fml.common.registry.IEntityAdditionalSpawnData;

public class EntityRobot extends EntityLivingBase implements
		IEntityAdditionalSpawnData {

	public LaserData laser = new LaserData ();
	private boolean needsUpdate = false;
	float curBlockDamage = 0;
	float buildEnergy = 0;

	private static ResourceLocation defaultTexture = new ResourceLocation(
			"buildcraft", DefaultProps.TEXTURE_PATH_ENTITIES
					+ "/robot_base.png");

	public AIBase currentAI;

	public class DockingStation {
		public int x, y, z;
		public ForgeDirection side;
	}

	public DockingStation dockingStation = new DockingStation();

	public EntityRobot(World par1World) {
		super(par1World);

		motionX = 0;
		motionY = 0;
		motionZ = 0;

		ignoreFrustumCheck = true;
		laser.isVisible = false;

		width = 0.5F;
		height = 0.5F;
	}

	@Override
	protected void entityInit() {
		super.entityInit();

		preventEntitySpawning = false;
		noClip = true;
		isImmuneToFire = true;

		dataWatcher.addObject(10, Float.valueOf(0));
		dataWatcher.addObject(11, Float.valueOf(0));
		dataWatcher.addObject(12, Float.valueOf(0));
		dataWatcher.addObject(13, Byte.valueOf((byte) 0));
		dataWatcher.addObject(14, Float.valueOf(0));
		dataWatcher.addObject(15, Float.valueOf(0));
		dataWatcher.addObject(16, Float.valueOf(0));
		dataWatcher.addObject(17, Byte.valueOf((byte) 0));
	}

	protected void updateDataClient() {
		laser.tail.x = dataWatcher.getWatchableObjectFloat(10);
		laser.tail.y = dataWatcher.getWatchableObjectFloat(11);
		laser.tail.z = dataWatcher.getWatchableObjectFloat(12);
		laser.isVisible = (dataWatcher.getWatchableObjectByte(13) == 1);
	}

	protected void updateDataServer() {
		dataWatcher.updateObject(10, Float.valueOf((float) laser.tail.x));
		dataWatcher.updateObject(11, Float.valueOf((float) laser.tail.y));
		dataWatcher.updateObject(12, Float.valueOf((float) laser.tail.z));
		dataWatcher.updateObject(13, Byte.valueOf((byte) (laser.isVisible ? 1 : 0)));
	}

	protected void init() {

	}

	public void setLaserDestination (float x, float y, float z) {
		if (x != laser.tail.x || y != laser.tail.y || z != laser.tail.z) {
			laser.tail.x = x;
			laser.tail.y = y;
			laser.tail.z = z;

			needsUpdate = true;
		}
	}

	public void showLaser () {
		if (laser != null && !laser.isVisible) {
			laser.isVisible = true;
			needsUpdate = true;
		}
	}

	public void hideLaser () {
		if (laser != null && laser.isVisible) {
			laser.isVisible = false;
			needsUpdate = true;
		}
	}

	@Override
	public void onUpdate() {
		if (!worldObj.isRemote && needsUpdate) {
			updateDataServer();
			needsUpdate = false;
		}

		if (worldObj.isRemote) {
			updateDataClient();
		}

		if (currentAI != null) {
			currentAI.update(this);
		}

		super.onUpdate();
	}

	public void setRegularBoundingBox () {
		width = 0.5F;
		height = 0.5F;

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
			boundingBox.minX = posX - 0.25F;
			boundingBox.minY = posY - 0.25F;
			boundingBox.minZ = posZ - 0.25F;

			boundingBox.maxX = posX + 0.25F;
			boundingBox.maxY = posY + 0.25F;
			boundingBox.maxZ = posZ + 0.25F;
		}
	}

	public void setNullBoundingBox () {
		width = 0F;
		height = 0F;

		boundingBox.minX = posX;
		boundingBox.minY = posY;
		boundingBox.minZ = posZ;

		boundingBox.maxX = posX;
		boundingBox.maxY = posY;
		boundingBox.maxZ = posZ;
	}

	private void iterateBehaviorDocked () {
		motionX = 0F;
		motionY = 0F;
		motionZ = 0F;

		setNullBoundingBox ();
	}

	protected void move() {

	}

	@Override
	public void writeSpawnData(ByteBuf data) {

	}

	@Override
	public void readSpawnData(ByteBuf data) {
		init();
	}

	@Override
	public ItemStack getHeldItem() {
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

	public ResourceLocation getTexture () {
		return defaultTexture;
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound par1NBTTagCompound) {
		super.readEntityFromNBT(par1NBTTagCompound);

		setDead();
    }

	public void setDockingStation (TileGenericPipe tile, ForgeDirection side) {
		dockingStation.x = tile.xCoord;
		dockingStation.y = tile.yCoord;
		dockingStation.z = tile.zCoord;
		dockingStation.side = side;
	}

	@Override
	public ItemStack getEquipmentInSlot(int var1) {
		// TODO Auto-generated method stub
		return null;
	}
}
