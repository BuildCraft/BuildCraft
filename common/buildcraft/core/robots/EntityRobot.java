/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.robots;

import java.util.Date;
import java.util.LinkedList;
import java.util.WeakHashMap;

import io.netty.buffer.ByteBuf;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import cpw.mods.fml.common.registry.IEntityAdditionalSpawnData;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import net.minecraftforge.common.util.Constants;

import buildcraft.BuildCraftSilicon;
import buildcraft.api.boards.RedstoneBoardNBT;
import buildcraft.api.boards.RedstoneBoardRegistry;
import buildcraft.api.boards.RedstoneBoardRobot;
import buildcraft.api.boards.RedstoneBoardRobotNBT;
import buildcraft.api.core.IZone;
import buildcraft.api.core.SafeTimeTracker;
import buildcraft.api.mj.MjBattery;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.api.robots.IDockingStation;
import buildcraft.commander.StackRequest;
import buildcraft.core.DefaultProps;
import buildcraft.core.LaserData;
import buildcraft.core.network.RPC;
import buildcraft.core.network.RPCHandler;
import buildcraft.core.network.RPCMessageInfo;
import buildcraft.core.network.RPCSide;
import buildcraft.core.utils.NBTUtils;
import buildcraft.silicon.statements.ActionRobotWorkInArea;
import buildcraft.transport.gates.ActionIterator;
import buildcraft.transport.gates.ActionSlot;

public class EntityRobot extends EntityRobotBase implements
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

	public LaserData laser = new LaserData();
	public IDockingStation reservedDockingStation;
	public IDockingStation linkedDockingStation;
	public boolean isDocked = false;

	public NBTTagCompound originalBoardNBT;
	public RedstoneBoardRobot board;
	public AIRobotMain mainAI;

	public ItemStack itemInUse;
	public float itemAngle1 = 0;
	public float itemAngle2 = 0;
	public boolean itemActive = false;
	public float itemActiveStage = 0;
	public long lastUpdateTime = 0;

	private boolean needsUpdate = false;
	private ItemStack[] inv = new ItemStack[4];
	private String boardID;
	private ResourceLocation texture;
	private IDockingStation currentDockingStation;
	private WeakHashMap<Entity, Boolean> unreachableEntities = new WeakHashMap<Entity, Boolean>();

	private NBTTagList stackRequestNBT;
	private LinkedList<StackRequest> stackRequests = new LinkedList<StackRequest>();

	@MjBattery
	private double mjStored;

	private boolean firstUpdateDone = false;

	public EntityRobot(World world, NBTTagCompound boardNBT) {
		this(world);

		originalBoardNBT = boardNBT;
		board = (RedstoneBoardRobot) RedstoneBoardRegistry.instance.getRedstoneBoard(boardNBT).create(boardNBT, this);
		dataWatcher.updateObject(16, board.getNBTHandler().getID());

		if (!world.isRemote) {
			mainAI = new AIRobotMain(this);
			mainAI.start();
		}
	}

	public EntityRobot(World world) {
		super(world);

		motionX = 0;
		motionY = 0;
		motionZ = 0;

		ignoreFrustumCheck = true;
		laser.isVisible = false;
		entityCollisionReduction = 1F;

		width = 0.25F;
		height = 0.25F;
	}

	@Override
	protected void entityInit() {
		super.entityInit();

		setNullBoundingBox();

		preventEntitySpawning = false;
		noClip = true;
		isImmuneToFire = true;

		dataWatcher.addObject(12, Float.valueOf(0));
		dataWatcher.addObject(13, Float.valueOf(0));
		dataWatcher.addObject(14, Float.valueOf(0));
		dataWatcher.addObject(15, Byte.valueOf((byte) 0));
		dataWatcher.addObject(16, "");
		dataWatcher.addObject(17, Float.valueOf(0));
		dataWatcher.addObject(18, Float.valueOf(0));
	}

	protected void updateDataClient() {
		laser.tail.x = dataWatcher.getWatchableObjectFloat(12);
		laser.tail.y = dataWatcher.getWatchableObjectFloat(13);
		laser.tail.z = dataWatcher.getWatchableObjectFloat(14);
		laser.isVisible = dataWatcher.getWatchableObjectByte(15) == 1;

		RedstoneBoardNBT<?> boardNBT = RedstoneBoardRegistry.instance.getRedstoneBoard(dataWatcher
				.getWatchableObjectString(16));

		if (boardNBT != null) {
			texture = ((RedstoneBoardRobotNBT) boardNBT).getRobotTexture();
		}

		itemAngle1 = dataWatcher.getWatchableObjectFloat(17);
		itemAngle2 = dataWatcher.getWatchableObjectFloat(18);
	}

	protected void updateDataServer() {
		dataWatcher.updateObject(12, Float.valueOf((float) laser.tail.x));
		dataWatcher.updateObject(13, Float.valueOf((float) laser.tail.y));
		dataWatcher.updateObject(14, Float.valueOf((float) laser.tail.z));
		dataWatcher.updateObject(15, Byte.valueOf((byte) (laser.isVisible ? 1 : 0)));
		dataWatcher.updateObject(17, Float.valueOf(itemAngle1));
		dataWatcher.updateObject(18, Float.valueOf(itemAngle2));
	}

	protected void init() {
		if (worldObj.isRemote) {
			RPCHandler.rpcServer(this, "requestInitialization");
		}
	}

	public void setLaserDestination (float x, float y, float z) {
		if (x != laser.tail.x || y != laser.tail.y || z != laser.tail.z) {
			laser.tail.x = x;
			laser.tail.y = y;
			laser.tail.z = z;

			needsUpdate = true;
		}
	}

	public void showLaser() {
		if (!laser.isVisible) {
			laser.isVisible = true;
			needsUpdate = true;
		}
	}

	public void hideLaser() {
		if (laser.isVisible) {
			laser.isVisible = false;
			needsUpdate = true;
		}
	}

	protected void firstUpdate() {
		if (stackRequestNBT != null) {

		}
	}

	@Override
	public void onUpdate() {
		if (!firstUpdateDone) {
			firstUpdate();
			firstUpdateDone = true;
		}

		if (!worldObj.isRemote && needsUpdate) {
			updateDataServer();
			needsUpdate = false;
		}

		if (worldObj.isRemote) {
			updateDataClient();
		}

		if (currentDockingStation != null) {
			motionX = 0;
			motionY = 0;
			motionZ = 0;
			posX = currentDockingStation.x() + 0.5F + currentDockingStation.side().offsetX * 0.5F;
			posY = currentDockingStation.y() + 0.5F + currentDockingStation.side().offsetY * 0.5F;
			posZ = currentDockingStation.z() + 0.5F + currentDockingStation.side().offsetZ * 0.5F;
		}

		if (!worldObj.isRemote) {
			if (linkedDockingStation instanceof FakeDockingStation) {
				// try to load the docking station. If the chunk can be loaded
				// but not the docking station, then the expecting docking
				// station is not around, kill the robot.
				IDockingStation station = ((FakeDockingStation) linkedDockingStation).getRealStation(worldObj);

				if (station != null) {
					linkToStation(station);
				} else {
					setDead();
				}
			}

			if (reservedDockingStation instanceof FakeDockingStation) {
				IDockingStation station = ((FakeDockingStation) reservedDockingStation).getRealStation(worldObj);

				if (station != null) {
					reserveStation(station);
				}
			}

			if (currentDockingStation instanceof FakeDockingStation) {
				currentDockingStation = ((FakeDockingStation) currentDockingStation).getRealStation(worldObj);
			}

			if (linkedDockingStation != null) {
				mainAI.cycle();

				if (mjStored <= 0) {
					setDead();
				}
			}
		}

		super.onUpdate();
	}

	public void setRegularBoundingBox() {
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

	public void setNullBoundingBox() {
		width = 0F;
		height = 0F;

		boundingBox.minX = posX;
		boundingBox.minY = posY;
		boundingBox.minZ = posZ;

		boundingBox.maxX = posX;
		boundingBox.maxY = posY;
		boundingBox.maxZ = posZ;
	}

	private void iterateBehaviorDocked() {
		motionX = 0F;
		motionY = 0F;
		motionZ = 0F;

		setNullBoundingBox ();
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
		return itemInUse;
	}

	@Override
	public void setCurrentItemOrArmor(int i, ItemStack itemstack) {
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

	public ResourceLocation getTexture() {
		return texture;
	}

	@Override
    public void writeEntityToNBT(NBTTagCompound nbt) {
		super.writeEntityToNBT(nbt);

		NBTTagCompound linkedStationNBT = new NBTTagCompound();
		linkedDockingStation.writeToNBT(linkedStationNBT);
		nbt.setTag("linkedStation", linkedStationNBT);

		if (reservedDockingStation != null) {
			NBTTagCompound stationNBT = new NBTTagCompound();
			reservedDockingStation.writeToNBT(stationNBT);
			nbt.setTag("reservedStation", stationNBT);
		}

		if (currentDockingStation != null) {
			NBTTagCompound stationNBT = new NBTTagCompound();
			currentDockingStation.writeToNBT(stationNBT);
			nbt.setTag("currentStation", stationNBT);
		}

		NBTTagCompound nbtLaser = new NBTTagCompound();
		laser.writeToNBT(nbtLaser);
		nbt.setTag("laser", nbtLaser);

		nbt.setDouble("mjStored", mjStored);

		if (itemInUse != null) {
			NBTTagCompound itemNBT = new NBTTagCompound();
			itemInUse.writeToNBT(itemNBT);
			nbt.setTag("itemInUse", itemNBT);
			nbt.setBoolean("itemActive", itemActive);
		}

		for (int i = 0; i < inv.length; ++i) {
			NBTTagCompound stackNbt = new NBTTagCompound();

			if (inv[i] != null) {
				nbt.setTag("inv[" + i + "]", inv[i].writeToNBT(stackNbt));
			}
		}

		nbt.setTag("originalBoardNBT", originalBoardNBT);

		NBTTagCompound ai = new NBTTagCompound();
		mainAI.writeToNBT(ai);
		nbt.setTag("mainAI", ai);

		if (mainAI.getDelegateAI() != board) {
			NBTTagCompound boardNBT = new NBTTagCompound();
			board.writeToNBT(boardNBT);
			nbt.setTag("board", boardNBT);
		}

		NBTTagList requestsNBT = new NBTTagList();

		for (StackRequest r : stackRequests) {
			NBTTagCompound cpt = new NBTTagCompound();

			NBTTagCompound index = new NBTTagCompound();
			r.holder.writeTo(index);
			cpt.setTag("index", index);

			cpt.setInteger("indexInHolder", r.indexInHolder);

			requestsNBT.appendTag(cpt);
		}

		nbt.setTag("stackRequests", requestsNBT);
    }

	@Override
	public void readEntityFromNBT(NBTTagCompound nbt) {
		super.readEntityFromNBT(nbt);

		linkedDockingStation = new FakeDockingStation();
		linkedDockingStation.readFromNBT(nbt.getCompoundTag("linkedStation"));

		if (nbt.hasKey("reservedStation")) {
			reservedDockingStation = new FakeDockingStation();
			reservedDockingStation.readFromNBT(nbt.getCompoundTag("reservedStation"));
		}

		if (nbt.hasKey("currentStation")) {
			currentDockingStation = new FakeDockingStation();
			currentDockingStation.readFromNBT(nbt.getCompoundTag("currentStation"));
		}

		laser.readFromNBT(nbt.getCompoundTag("laser"));

		mjStored = nbt.getDouble("mjStored");

		if (nbt.hasKey("itemInUse")) {
			itemInUse = ItemStack.loadItemStackFromNBT(nbt.getCompoundTag("itemInUse"));
			itemActive = nbt.getBoolean("itemActive");
		}

		for (int i = 0; i < inv.length; ++i) {
			inv[i] = ItemStack.loadItemStackFromNBT(nbt.getCompoundTag("inv[" + i + "]"));
		}

		originalBoardNBT = nbt.getCompoundTag("originalBoardNBT");

		NBTTagCompound ai = nbt.getCompoundTag("mainAI");
		mainAI = (AIRobotMain) AIRobot.loadAI(ai, this);

		if (nbt.hasKey("board")) {
			board = (RedstoneBoardRobot) AIRobot.loadAI(nbt.getCompoundTag("board"), this);
		} else {
			board = (RedstoneBoardRobot) mainAI.getDelegateAI();
		}

		dataWatcher.updateObject(16, board.getNBTHandler().getID());

		stackRequestNBT = nbt.getTagList("stackRequests", Constants.NBT.TAG_COMPOUND);
    }

	@Override
	public void dock(IDockingStation station) {
		currentDockingStation = station;
	}

	@Override
	public void undock() {
		if (currentDockingStation != null && currentDockingStation instanceof DockingStation) {
			((DockingStation) currentDockingStation).unreserve(this);
			currentDockingStation = null;
		}
	}

	@Override
	public DockingStation getDockingStation() {
		return (DockingStation) currentDockingStation;
	}

	@Override
	public boolean reserveStation(IDockingStation iStation) {
		DockingStation station = (DockingStation) iStation;

		if (station == null) {
			if (reservedDockingStation != null && reservedDockingStation instanceof DockingStation) {
				((DockingStation) reservedDockingStation).unreserve(this);
			}

			reservedDockingStation = null;

			return true;
		} else if (station.reserved() == this) {
			return true;
		} else if (station.reserve(this)) {
			if (reservedDockingStation != null && reservedDockingStation instanceof DockingStation) {
				((DockingStation) reservedDockingStation).unreserve(this);
			}

			reservedDockingStation = station;
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean linkToStation(IDockingStation iStation) {
		DockingStation station = (DockingStation) iStation;

		if (station.linked() == this) {
			return true;
		} else if (station.link(this)) {
			if (linkedDockingStation != null && linkedDockingStation instanceof DockingStation) {
				((DockingStation) linkedDockingStation).unlink(this);
			}

			linkedDockingStation = station;
			return true;
		} else {
			return false;
		}
	}

	@Override
	public ItemStack getEquipmentInSlot(int var1) {
		return null;
	}

	public boolean acceptTask (IRobotTask task) {
		return false;
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
						+ var2.stackSize <= inv[var1].getItem().getItemStackLimit());
	}

	@Override
	public boolean isMoving() {
		return motionX != 0 || motionY != 0 || motionZ != 0;
	}

	@Override
	public void setItemInUse(ItemStack stack) {
		itemInUse = stack;
		RPCHandler.rpcBroadcastWorldPlayers(worldObj, this, "clientSetItemInUse", stack);
	}

	@RPC(RPCSide.CLIENT)
	private void clientSetItemInUse(ItemStack stack) {
		itemInUse = stack;
	}

	@RPC(RPCSide.SERVER)
	public void requestInitialization(RPCMessageInfo info) {
		RPCHandler.rpcPlayer(info.sender, this, "rpcInitialize", itemInUse, itemActive);
	}

	@RPC(RPCSide.CLIENT)
	private void rpcInitialize(ItemStack stack, boolean active) {
		itemInUse = stack;
		itemActive = active;
	}

	@Override
	public void setHealth(float par1) {
		// deactivate healh management
	}

	@Override
	public void aimItemAt(int x, int y, int z) {
		itemAngle1 = (float) Math.atan2(z - Math.floor(posZ),
				x - Math.floor(posX));

		itemAngle2 = 0;

		if (Math.floor(posY) < y) {
			itemAngle2 = (float) -Math.PI / 4;

			if (Math.floor(posX) == x && Math.floor(posZ) == z) {
				itemAngle2 -= (float) Math.PI / 4;
			}
		} else if (Math.floor(posY) > y) {
			itemAngle2 = (float) Math.PI / 2;

			if (Math.floor(posX) == x && Math.floor(posZ) == z) {
				itemAngle2 += (float) Math.PI / 4;
			}
		}

		updateDataServer();
	}

	@Override
	public void setItemActive(boolean isActive) {
		if (isActive != itemActive) {
			itemActive = isActive;
			RPCHandler.rpcBroadcastWorldPlayers(worldObj, this, "rpcSetItemActive", isActive);
		}
	}

	@RPC(RPCSide.CLIENT)
	private void rpcSetItemActive(boolean isActive) {
		itemActive = isActive;
		itemActiveStage = 0;
		lastUpdateTime = new Date().getTime();
	}

	@Override
	public RedstoneBoardRobot getBoard() {
		return board;
	}

	@Override
	public IDockingStation getLinkedStation() {
		return linkedDockingStation;
	}

	@Override
	public IDockingStation getReservedStation() {
		return reservedDockingStation;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public boolean isInRangeToRenderDist(double par1) {
		return true;
    }

	@Override
	public double getEnergy() {
		return mjStored;
	}

	@Override
	public void setEnergy(double energy) {
		mjStored = energy;

		if (mjStored > MAX_ENERGY) {
			mjStored = MAX_ENERGY;
		}
	}

	@Override
	protected boolean canDespawn() {
		return false;
	}

	public AIRobot getOverridingAI() {
		return mainAI.getOverridingAI();
	}

	public void overrideAI(AIRobot ai) {
		mainAI.setOverridingAI(ai);
	}

	public void attackTargetEntityWithCurrentItem(Entity par1Entity) {
		ItemStack stack = itemInUse;

		if (par1Entity.canAttackWithItem()) {
			if (!par1Entity.hitByEntity(this)) {
				this.setLastAttacker(par1Entity);
				boolean flag2 = par1Entity.attackEntityFrom(new EntityDamageSource("robot", this), 2.0F);

				EnchantmentHelper.func_151385_b(this, par1Entity);
				ItemStack itemstack = itemInUse;
				Object object = par1Entity;

				if (itemstack != null && object instanceof EntityLivingBase) {
					itemstack.getItem().hitEntity(itemstack, (EntityLivingBase) object, this);
				}

			}
		}
	}

	@Override
	public IZone getZoneToWork() {
		if (linkedDockingStation instanceof DockingStation) {
			for (ActionSlot s : new ActionIterator(((DockingStation) linkedDockingStation).pipe.pipe)) {
				if (s.action instanceof ActionRobotWorkInArea) {
					IZone zone = ActionRobotWorkInArea.getArea(s);

					if (zone != null) {
						return zone;
					}
				}
			}
		}

		return null;
	}

	@Override
	public boolean containsItems() {
		for (ItemStack element : inv) {
			if (element != null) {
				return true;
			}
		}

		return false;
	}

	@Override
	public void unreachableEntityDetected(Entity entity) {
		unreachableEntities.put(entity, true);
	}

	@Override
	public boolean isKnownUnreachable(Entity entity) {
		return unreachableEntities.containsKey(entity);
	}

	@Override
	public void setDead() {
		if (!worldObj.isRemote && !isDead) {
			// FIXME: placing a robot gives it full energy, so if it's dropped
			// for lack of energy, it's a cheap way to refuel it. Find
			// some other way to cope with that problem - such as a manual
			// charger?

			mainAI.abort();

			ItemStack robotStack = new ItemStack (BuildCraftSilicon.robotItem);
			NBTUtils.getItemData(robotStack).setTag("board", originalBoardNBT);
			entityDropItem(robotStack, 0);

			if (linkedDockingStation != null && linkedDockingStation instanceof DockingStation) {
				((DockingStation) linkedDockingStation).unlink(this);
			}

			if (reservedDockingStation != null && reservedDockingStation instanceof DockingStation) {
				((DockingStation) reservedDockingStation).unreserve(this);
			}
		}

		super.setDead();
	}

	@Override
	public boolean canBePushed() {
		return false;
	}

	@Override
	protected void collideWithEntity(Entity par1Entity) {

	}

	@Override
	public void applyEntityCollision(Entity par1Entity) {

	}
}
