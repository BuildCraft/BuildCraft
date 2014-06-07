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

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import cpw.mods.fml.common.registry.IEntityAdditionalSpawnData;

import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.api.boards.IRedstoneBoardRobot;
import buildcraft.api.boards.RedstoneBoardNBT;
import buildcraft.api.boards.RedstoneBoardRegistry;
import buildcraft.api.boards.RedstoneBoardRobotNBT;
import buildcraft.api.core.SafeTimeTracker;
import buildcraft.core.DefaultProps;
import buildcraft.core.LaserData;
import buildcraft.transport.TileGenericPipe;

public class EntityRobot extends EntityLiving implements
		IEntityAdditionalSpawnData, IInventory {

	public static final ResourceLocation ROBOT_BASE = new ResourceLocation("buildcraft",
			DefaultProps.TEXTURE_PATH_ENTITIES + "/robot_base.png");
	public static final ResourceLocation ROBOT_BUILDER = new ResourceLocation("buildcraft",
			DefaultProps.TEXTURE_PATH_ENTITIES + "/robot_builder.png");
	public static final ResourceLocation ROBOT_TRANSPORT = new ResourceLocation("buildcraft",
			DefaultProps.TEXTURE_PATH_ENTITIES + "/robot_picker.png");
	public static final ResourceLocation ROBOT_FACTORY = new ResourceLocation("buildcraft",
			DefaultProps.TEXTURE_PATH_ENTITIES + "/robot_factory.png");

	private static ResourceLocation defaultTexture = new ResourceLocation("buildcraft", DefaultProps.TEXTURE_PATH_ENTITIES + "/robot_base.png");

	public SafeTimeTracker scanForTasks = new SafeTimeTracker (40, 10);

	public LaserData laser = new LaserData ();
	public IRobotTask currentTask;
	public DockingStation dockingStation = new DockingStation();
	public boolean isDocked = false;

	public IRedstoneBoardRobot board;

	public RobotAIBase currentAI;
	protected RobotAIBase nextAI;

	private boolean needsUpdate = false;
	private ItemStack[] inv = new ItemStack[6];
	private String boardID;
	private ResourceLocation texture;

	public class DockingStation {
		public int x, y, z;
		public ForgeDirection side;
	}

	public EntityRobot(World world, IRedstoneBoardRobot iBoard) {
		this(world);

		board = iBoard;
		dataWatcher.updateObject(16, board.getNBTHandler().getID());
	}

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

		dataWatcher.addObject(12, Float.valueOf(0));
		dataWatcher.addObject(13, Float.valueOf(0));
		dataWatcher.addObject(14, Float.valueOf(0));
		dataWatcher.addObject(15, Byte.valueOf((byte) 0));
		dataWatcher.addObject(16, "");
	}

	protected void updateDataClient() {
		laser.tail.x = dataWatcher.getWatchableObjectFloat(12);
		laser.tail.y = dataWatcher.getWatchableObjectFloat(13);
		laser.tail.z = dataWatcher.getWatchableObjectFloat(14);
		laser.isVisible = dataWatcher.getWatchableObjectByte(15) == 1;

		RedstoneBoardNBT boardNBT = RedstoneBoardRegistry.instance.getRedstoneBoard(dataWatcher
				.getWatchableObjectString(16));

		if (boardNBT != null) {
			texture = ((RedstoneBoardRobotNBT) boardNBT).getRobotTexture();
		}
	}

	protected void updateDataServer() {
		dataWatcher.updateObject(12, Float.valueOf((float) laser.tail.x));
		dataWatcher.updateObject(13, Float.valueOf((float) laser.tail.y));
		dataWatcher.updateObject(14, Float.valueOf((float) laser.tail.z));
		dataWatcher.updateObject(15, Byte.valueOf((byte) (laser.isVisible ? 1 : 0)));
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
		if (!laser.isVisible) {
			laser.isVisible = true;
			needsUpdate = true;
		}
	}

	public void hideLaser () {
		if (laser.isVisible) {
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

		if (nextAI != null) {
			if (currentAI != null) {
				tasks.removeTask(currentAI);
			}

			currentAI = nextAI;
			nextAI = null;
			tasks.addTask(0, currentAI);
		}

		if (!worldObj.isRemote) {
			board.updateBoard(this);

			if (currentTask == null) {
				if (scanForTasks.markTimeIfDelay(worldObj)) {
					RobotTaskProviderRegistry.scanForTask(this);
				}
			} else {
				if (currentTask.done()) {
					currentTask = null;
				} else {
					currentTask.update(this);
				}
			}
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
		return texture;
	}

	@Override
    public void writeEntityToNBT(NBTTagCompound nbt) {
		super.writeEntityToNBT(nbt);

		nbt.setInteger("dockX", dockingStation.x);
		nbt.setInteger("dockY", dockingStation.y);
		nbt.setInteger("dockZ", dockingStation.z);
		nbt.setInteger("dockSide", dockingStation.side.ordinal());

		if (currentAI != null) {
			nbt.setString("ai", currentAI.getClass().getCanonicalName());
		}

		NBTTagCompound nbtLaser = new NBTTagCompound();
		laser.writeToNBT(nbtLaser);
		nbt.setTag("laser", nbtLaser);

		for (int i = 0; i < inv.length; ++i) {
			NBTTagCompound stackNbt = new NBTTagCompound();

			if (inv[i] != null) {
				nbt.setTag("inv[" + i + "]", inv[i].writeToNBT(stackNbt));
			}
		}
    }

	@Override
	public void readEntityFromNBT(NBTTagCompound nbt) {
		super.readEntityFromNBT(nbt);

		dockingStation.x = nbt.getInteger("dockX");
		dockingStation.y = nbt.getInteger("dockY");
		dockingStation.z = nbt.getInteger("dockZ");
		dockingStation.side = ForgeDirection.values () [nbt.getInteger("dockSide")];

		/*
		 * if (nbt.hasKey("ai")) { try { nextAI = (RobotAIBase)
		 * Class.forName(nbt.getString("ai")).newInstance(); } catch (Throwable
		 * t) { t.printStackTrace(); } }
		 */

		laser.readFromNBT(nbt.getCompoundTag("laser"));

		for (int i = 0; i < inv.length; ++i) {
			inv[i] = ItemStack.loadItemStackFromNBT(nbt.getCompoundTag("inv[" + i + "]"));
		}

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

	public boolean acceptTask (IRobotTask task) {
		return false;
	}

	@Override
	protected boolean isAIEnabled() {
		return true;
	}

	public void setMainAI (RobotAIBase ai) {
		nextAI = ai;
	}

	@Override
	public int getSizeInventory() {
		return inv.length;
	}

	@Override
	public ItemStack getStackInSlot(int var1) {
		return inv[var1];
	}

	@Override
	public ItemStack decrStackSize(int var1, int var2) {
		ItemStack result = inv[var1].splitStack(var2);

		if (inv[var1].stackSize == 0) {
			inv[var1] = null;
		}

		return result;
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int var1) {
		return inv[var1].splitStack(var1);
	}

	@Override
	public void setInventorySlotContents(int var1, ItemStack var2) {
		inv[var1] = var2;
	}

	@Override
	public String getInventoryName() {
		return null;
	}

	@Override
	public boolean hasCustomInventoryName() {
		return false;
	}

	@Override
	public int getInventoryStackLimit() {
		return 64;
	}

	@Override
	public void markDirty() {
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer var1) {
		return false;
	}

	@Override
	public void openInventory() {
	}

	@Override
	public void closeInventory() {
	}

	@Override
	public boolean isItemValidForSlot(int var1, ItemStack var2) {
		return inv[var1] == null
				|| (inv[var1].isItemEqual(var2) && inv[var1].isStackable() && inv[var1].stackSize
						+ var2.stackSize <= inv[var1].getItem()
						.getItemStackLimit());
	}

	public boolean isMoving() {
		return motionX != 0 || motionY != 0 || motionZ != 0;
	}
}
