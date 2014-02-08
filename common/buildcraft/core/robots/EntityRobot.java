/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.robots;

import buildcraft.builders.blueprints.IBlueprintBuilderAgent;
import buildcraft.core.DefaultProps;
import buildcraft.core.LaserData;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.transport.TileGenericPipe;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import cpw.mods.fml.common.registry.IEntityAdditionalSpawnData;

public class EntityRobot extends EntityLivingBase implements
		IEntityAdditionalSpawnData, IBlueprintBuilderAgent, IInventory {

	public LaserData laser = new LaserData ();
	private boolean needsUpdate = false;
	float curBlockDamage = 0;
	float buildEnergy = 0;
	ItemStack buildingStack = null;

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
		if (CoreProxy.proxy.isSimulating(worldObj) && needsUpdate) {
			updateDataServer();
			needsUpdate = false;
		}

		if (CoreProxy.proxy.isRenderWorld(worldObj)) {
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

	/**
	 * Operate a block break. Return true is the block has indeed been broken.
	 */
	@Override
	public boolean breakBlock (int x, int y, int z) {
		Block block = Block.blocksList[worldObj.getBlockId(x, y, z)];

		if (block != null) {
			curBlockDamage += 1 / (block.getBlockHardness(worldObj, x,y, z) * 20);
		}

		if (block != null && curBlockDamage < 1) {
			worldObj.destroyBlockInWorldPartially(entityId, x, y, z,
					(int) (this.curBlockDamage * 10.0F) - 1);

			setLaserDestination(x + 0.5F, y + 0.5F, z + 0.5F);
			showLaser();

			return false;
		} else {
			worldObj.destroyBlockInWorldPartially(entityId, x, y, z, -1);
			worldObj.setBlock(x, y, z, 0);
			curBlockDamage = 0;

			hideLaser();

			return true;
		}
	}

	@Override
	public boolean buildBlock(int x, int y, int z) {
		if (buildingStack == null) {
			if (worldObj.getBlockId(x, y, z) != 0) {
				breakBlock(x, y, z);
			} else {
				setLaserDestination((float) x + 0.5F, (float) y + 0.5F, (float) z + 0.5F);
				showLaser();

				buildingStack = getInventory().decrStackSize(0, 1);
				buildEnergy = 0;
			}

			return false;
		} else {
			buildEnergy++;

			if (buildEnergy >= 25) {
				buildingStack.getItem().onItemUse(buildingStack,
						CoreProxy.proxy.getBuildCraftPlayer(worldObj),
						worldObj, x, y - 1, z, 1, 0.0f, 0.0f, 0.0f);

				buildingStack = null;

				hideLaser();
				return true;
			} else {
				return false;
			}
		}
	}

	@Override
	public IInventory getInventory() {
		return this;
	}

	@Override
	public int getSizeInventory() {
		return 1;
	}

	@Override
	public ItemStack getStackInSlot(int i) {
		// Fake inventory filled with bricks
		return new ItemStack(Block.brick);
	}

	@Override
	public ItemStack decrStackSize(int i, int j) {
		// Fake inventory filled with bricks
		return new ItemStack(Block.brick);
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int i) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack itemstack) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getInvName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isInvNameLocalized() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int getInventoryStackLimit() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void onInventoryChanged() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer entityplayer) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void openChest() {
		// TODO Auto-generated method stub

	}

	@Override
	public void closeChest() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemstack) {
		// TODO Auto-generated method stub
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
}
