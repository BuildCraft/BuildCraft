/**
 * Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.robots;

import java.util.Date;
import java.util.WeakHashMap;

import io.netty.buffer.ByteBuf;

import net.minecraft.client.Minecraft;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import cpw.mods.fml.common.registry.IEntityAdditionalSpawnData;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

import buildcraft.BuildCraftCore;
import buildcraft.BuildCraftSilicon;
import buildcraft.api.boards.RedstoneBoardNBT;
import buildcraft.api.boards.RedstoneBoardRegistry;
import buildcraft.api.boards.RedstoneBoardRobot;
import buildcraft.api.boards.RedstoneBoardRobotNBT;
import buildcraft.api.core.BlockIndex;
import buildcraft.api.core.IZone;
import buildcraft.api.core.SafeTimeTracker;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.api.robots.IDockingStation;
import buildcraft.core.DefaultProps;
import buildcraft.core.LaserData;
import buildcraft.core.RFBattery;
import buildcraft.core.network.CommandWriter;
import buildcraft.core.network.ICommandReceiver;
import buildcraft.core.network.PacketCommand;
import buildcraft.core.utils.NBTUtils;
import buildcraft.core.utils.Utils;
import buildcraft.robots.ai.AIRobotMain;
import buildcraft.robots.statements.ActionRobotWorkInArea;
import buildcraft.transport.PipeTransportPower;
import buildcraft.transport.gates.ActionIterator;
import buildcraft.transport.gates.StatementSlot;

public class EntityRobot extends EntityRobotBase implements
		IEntityAdditionalSpawnData, IInventory, IFluidHandler, ICommandReceiver {

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
	public IDockingStation linkedDockingStation;
	public BlockIndex linkedDockingStationIndex;
	public ForgeDirection linkedDockingStationSide;

	public BlockIndex currentDockingStationIndex;
	public ForgeDirection currentDockingStationSide;

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

	private DockingStation currentDockingStation;

	private boolean needsUpdate = false;
	private ItemStack[] inv = new ItemStack[4];
	private FluidStack tank;
	private int maxFluid = FluidContainerRegistry.BUCKET_VOLUME * 4;
	private String boardID;
	private ResourceLocation texture;

	private WeakHashMap<Entity, Boolean> unreachableEntities = new WeakHashMap<Entity, Boolean>();

	private NBTTagList stackRequestNBT;

	private RFBattery battery = new RFBattery(MAX_ENERGY, MAX_ENERGY, 100);

	private boolean firstUpdateDone = false;

	private long robotId = EntityRobotBase.NULL_ROBOT_ID;

	private int energySpendPerCycle = 0;
	private float energyFX = 0;
	private int steamDx = 0;
	private int steamDy = -1;
	private int steamDz = 0;

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
		this.func_110163_bv(); // persistenceRequired = true

		dataWatcher.addObject(12, Float.valueOf(0));
		dataWatcher.addObject(13, Float.valueOf(0));
		dataWatcher.addObject(14, Float.valueOf(0));
		dataWatcher.addObject(15, Byte.valueOf((byte) 0));
		dataWatcher.addObject(16, "");
		dataWatcher.addObject(17, Float.valueOf(0));
		dataWatcher.addObject(18, Float.valueOf(0));
		dataWatcher.addObject(19, Integer.valueOf(0));
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
		energySpendPerCycle = dataWatcher.getWatchableObjectInt(19);
	}

	protected void updateDataServer() {
		dataWatcher.updateObject(12, Float.valueOf((float) laser.tail.x));
		dataWatcher.updateObject(13, Float.valueOf((float) laser.tail.y));
		dataWatcher.updateObject(14, Float.valueOf((float) laser.tail.z));
		dataWatcher.updateObject(15, Byte.valueOf((byte) (laser.isVisible ? 1 : 0)));
		dataWatcher.updateObject(17, Float.valueOf(itemAngle1));
		dataWatcher.updateObject(18, Float.valueOf(itemAngle2));
		dataWatcher.updateObject(19, energySpendPerCycle);
	}

	protected void init() {
		if (worldObj.isRemote) {
			BuildCraftCore.instance.sendToServer(new PacketCommand(this, "requestInitialization", null));
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

		if (!worldObj.isRemote) {
			getRegistry().registerRobot(this);
		}
	}

	@Override
	public void onEntityUpdate() {
		this.worldObj.theProfiler.startSection("bcEntityRobot");
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

			energyFX += energySpendPerCycle;

			if (energyFX >= 100) {
				energyFX = 0;
				spawnEnergyFX();
			}
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
			if (linkedDockingStation == null) {
				linkedDockingStation = RobotRegistry.getRegistry(worldObj).getStation(
						linkedDockingStationIndex.x,
						linkedDockingStationIndex.y,
						linkedDockingStationIndex.z,
						linkedDockingStationSide);

				if (linkedDockingStation == null
						|| linkedDockingStation.robotTaking() != this) {
					// Error at load time, the expected linked stations is not
					// properly set, kill this robot.

					setDead();
					return;
				}
			}

			if (currentDockingStationIndex != null && currentDockingStation == null) {
				currentDockingStation = (DockingStation)
						RobotRegistry.getRegistry(worldObj).getStation(
						currentDockingStationIndex.x,
						currentDockingStationIndex.y,
						currentDockingStationIndex.z,
						currentDockingStationSide);
			}

			if (linkedDockingStation != null) {
				this.worldObj.theProfiler.startSection("bcRobotAIMainCycle");
				mainAI.cycle();
				this.worldObj.theProfiler.endSection();

				if (energySpendPerCycle != mainAI.getActiveAI().getEnergyCost()) {
					energySpendPerCycle = mainAI.getActiveAI().getEnergyCost();
					needsUpdate = true;
				}

				if (this.battery.getEnergyStored() <= 0 && !linkedToChargeStation()) {
					setDead();
				}
			}
		}

		super.onEntityUpdate();
		this.worldObj.theProfiler.endSection();
	}

	private boolean linkedToChargeStation() {
		if (currentDockingStation == null) {
			return false;
		}
		if (!(currentDockingStation.getPipe().pipe.transport instanceof PipeTransportPower)) {
			return false;
		}
		return true;
	}

	@SideOnly(Side.CLIENT)
	private void spawnEnergyFX() {
	    Minecraft.getMinecraft().effectRenderer.addEffect(new EntityRobotEnergyParticle(
                worldObj,
                posX + steamDx * 0.25, posY + steamDy * 0.25, posZ + steamDz * 0.25,
                steamDx * 0.05, steamDy * 0.05, steamDz * 0.05,
                energySpendPerCycle * 0.075F < 1 ? 1 : energySpendPerCycle * 0.075F));        
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
		NBTTagCompound linkedStationIndexNBT = new NBTTagCompound();
		linkedDockingStationIndex.writeTo(linkedStationIndexNBT);
		linkedStationNBT.setTag("index", linkedStationIndexNBT);
		linkedStationNBT.setByte("side", (byte) linkedDockingStationSide.ordinal());
		nbt.setTag("linkedStation", linkedStationNBT);

		if (currentDockingStationIndex != null) {
			NBTTagCompound currentStationNBT = new NBTTagCompound();
			NBTTagCompound currentStationIndexNBT = new NBTTagCompound();
			currentDockingStationIndex.writeTo(currentStationIndexNBT);
			currentStationNBT.setTag("index", currentStationIndexNBT);
			currentStationNBT.setByte("side", (byte) currentDockingStationSide.ordinal());
			nbt.setTag("currentStation", currentStationNBT);
		}

		NBTTagCompound nbtLaser = new NBTTagCompound();
		laser.writeToNBT(nbtLaser);
		nbt.setTag("laser", nbtLaser);

		NBTTagCompound batteryNBT = new NBTTagCompound();
		battery.writeToNBT(batteryNBT);
		nbt.setTag("battery", batteryNBT);

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

		nbt.setLong("robotId", robotId);

		if (tank != null) {
			NBTTagCompound tankNBT = new NBTTagCompound();

			tank.writeToNBT(tankNBT);

			nbt.setTag("tank", tankNBT);
		}
    }

	@Override
	public void readEntityFromNBT(NBTTagCompound nbt) {
		super.readEntityFromNBT(nbt);

		NBTTagCompound linkedStationNBT = nbt.getCompoundTag("linkedStation");
		linkedDockingStationIndex = new BlockIndex(linkedStationNBT.getCompoundTag("index"));
		linkedDockingStationSide = ForgeDirection.values()[linkedStationNBT.getByte("side")];

		if (nbt.hasKey("currentStation")) {
			NBTTagCompound currentStationNBT = nbt.getCompoundTag("currentStation");
			currentDockingStationIndex = new BlockIndex(currentStationNBT.getCompoundTag("index"));
			currentDockingStationSide = ForgeDirection.values()[currentStationNBT.getByte("side")];

		}

		laser.readFromNBT(nbt.getCompoundTag("laser"));

		battery.readFromNBT(nbt.getCompoundTag("battery"));

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

		if (nbt.hasKey("robotId")) {
			robotId = nbt.getLong("robotId");
		}

		if (nbt.hasKey("tank")) {
			tank = FluidStack.loadFluidStackFromNBT(nbt.getCompoundTag("tank"));
		} else {
			tank = null;
		}

		// Restore robot persistence on pre-6.1.9 robots
		this.func_110163_bv();
    }

	@Override
	public void dock(IDockingStation station) {
		currentDockingStation = (DockingStation) station;

		setSteamDirection(
				currentDockingStation.side.offsetX,
				currentDockingStation.side.offsetY,
				currentDockingStation.side.offsetZ);

		currentDockingStationIndex = currentDockingStation.index();
		currentDockingStationSide = currentDockingStation.side();
	}

	@Override
	public void undock() {
		if (currentDockingStation != null) {
			currentDockingStation.release(this);
			currentDockingStation = null;

			setSteamDirection(0, -1, 0);

			currentDockingStationIndex = null;
			currentDockingStationSide = null;
		}
	}

	@Override
	public DockingStation getDockingStation() {
		return currentDockingStation;
	}

	public void setMainStation(IDockingStation iStation) {
		DockingStation station = (DockingStation) iStation;

		if (linkedDockingStation != null && linkedDockingStation != station) {
			((DockingStation) linkedDockingStation).unsafeRelease(this);
		}

		linkedDockingStation = station;
		linkedDockingStationIndex = linkedDockingStation.index();
		linkedDockingStationSide = linkedDockingStation.side();
	}

	@Override
	public ItemStack getEquipmentInSlot(int var1) {
		return null;
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

		updateClientSlot(var1);

		return result;
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int var1) {
		return inv[var1].splitStack(var1);
	}

	@Override
	public void setInventorySlotContents(int var1, ItemStack var2) {
		inv[var1] = var2;

		updateClientSlot(var1);
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

	public void updateClientSlot(final int slot) {
		BuildCraftCore.instance.sendToWorld(new PacketCommand(this, "clientSetInventory", new CommandWriter() {
			public void write(ByteBuf data) {
				data.writeShort(slot);
				Utils.writeStack(data, inv[slot]);
			}
		}), worldObj);
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
						+ var2.stackSize <= inv[var1].getItem().getItemStackLimit(inv[var1]));
	}

	@Override
	public boolean isMoving() {
		return motionX != 0 || motionY != 0 || motionZ != 0;
	}

	@Override
	public void setItemInUse(ItemStack stack) {
		itemInUse = stack;
		BuildCraftCore.instance.sendToWorld(new PacketCommand(this, "clientSetItemInUse", new CommandWriter() {
			public void write(ByteBuf data) {
				Utils.writeStack(data, itemInUse);
			}
		}), worldObj);
	}

	private void setSteamDirection(final int x, final int y, final int z) {
		if (!worldObj.isRemote) {
			BuildCraftCore.instance.sendToWorld(new PacketCommand(this, "setSteamDirection", new CommandWriter() {
				public void write(ByteBuf data) {
					data.writeInt(x);
					data.writeShort(y);
					data.writeInt(z);
				}
			}), worldObj);
		} else {
			Vec3 v = Vec3.createVectorHelper(x, y, z);
			v = v.normalize();

			steamDx = (int) v.xCoord;
			steamDy = (int) v.yCoord;
			steamDz = (int) v.zCoord;
		}
	}

	@Override
	public void receiveCommand(String command, Side side, Object sender, ByteBuf stream) {
		if (side.isClient()) {
			if ("clientSetItemInUse".equals(command)) {
				itemInUse = Utils.readStack(stream);
			} else if ("clientSetInventory".equals(command)) {
				int slot = stream.readUnsignedShort();
				inv[slot] = Utils.readStack(stream);
			} else if ("initialize".equals(command)) {
				itemInUse = Utils.readStack(stream);
				itemActive = stream.readBoolean();
			} else if ("setItemActive".equals(command)) {
				itemActive = stream.readBoolean();
				itemActiveStage = 0;
				lastUpdateTime = new Date().getTime();

				if (!itemActive) {
					setSteamDirection(0, -1, 0);
				}
			} else if ("setSteamDirection".equals(command)) {
				setSteamDirection(stream.readInt(), stream.readShort(), stream.readInt());
			}
		} else if (side.isServer()) {
			EntityPlayer p = (EntityPlayer) sender;
			if ("requestInitialization".equals(command)) {
				BuildCraftCore.instance.sendToPlayer(p, new PacketCommand(this, "initialize", new CommandWriter() {
					public void write(ByteBuf data) {
						Utils.writeStack(data, itemInUse);
						data.writeBoolean(itemActive);
					}
				}));

				for (int i = 0; i < inv.length; ++i) {
					final int j = i;
					BuildCraftCore.instance.sendToPlayer(p, new PacketCommand(this, "clientSetInventory", new CommandWriter() {
						public void write(ByteBuf data) {
							data.writeShort(j);
							Utils.writeStack(data, inv[j]);
						}
					}));
				}

				if (currentDockingStation != null) {
					setSteamDirection(
							currentDockingStation.side.offsetX,
							currentDockingStation.side.offsetY,
							currentDockingStation.side.offsetZ);
				} else {
					setSteamDirection(0, -1, 0);
				}
			}
		}
	}

	@Override
	public void setHealth(float par1) {
		// deactivate health management
	}

	@Override
	public boolean attackEntityFrom(DamageSource par1, float par2) {
		// deactivate being hit
		return false;
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

		int xComp = (int) Math.floor(posX);
		int yComp = (int) Math.floor(posY);
		int zComp = (int) Math.floor(posZ);

		setSteamDirection(xComp - x, yComp - y, zComp - z);

		updateDataServer();
	}

	@Override
	public void setItemActive(final boolean isActive) {
		if (isActive != itemActive) {
			itemActive = isActive;
			BuildCraftCore.instance.sendToWorld(new PacketCommand(this, "setItemActive", new CommandWriter() {
				public void write(ByteBuf data) {
					data.writeBoolean(isActive);
				}
			}), worldObj);
		}
	}

	@Override
	public RedstoneBoardRobot getBoard() {
		return board;
	}

	@Override
	public IDockingStation getLinkedStation() {
		return linkedDockingStation;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public boolean isInRangeToRenderDist(double par1) {
		return true;
    }

	@Override
	public int getEnergy() {
		return battery.getEnergyStored();
	}

	@Override
	public RFBattery getBattery() {
		return battery;
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
			for (StatementSlot s : new ActionIterator(((DockingStation) linkedDockingStation).getPipe().pipe)) {
				if (s.statement instanceof ActionRobotWorkInArea) {
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
	public boolean hasFreeSlot() {
		for (ItemStack element : inv) {
			if (element == null) {
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
			if (mainAI != null) {
				mainAI.abort();
			}

			ItemStack robotStack = new ItemStack (BuildCraftSilicon.robotItem);
			NBTUtils.getItemData(robotStack).setTag("board", originalBoardNBT);
			NBTUtils.getItemData(robotStack).setInteger("energy", battery.getEnergyStored());
			entityDropItem(robotStack, 0);
			if (itemInUse != null) {
				entityDropItem(itemInUse, 0);
			}
			for (ItemStack element : inv) {
				if (element != null) {
					entityDropItem(element, 0);
				}
			}

			getRegistry().killRobot(this);
		}

		super.setDead();
	}

	@Override
	public void onChunkUnload() {
		getRegistry().unloadRobot(this);
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

	public void setUniqueRobotId(long iRobotId) {
		robotId = iRobotId;
	}

	@Override
	public long getRobotId() {
		return robotId;
	}

	@Override
	public RobotRegistry getRegistry() {
		return RobotRegistry.getRegistry(worldObj);
	}

	@Override
	public void releaseResources() {
		getRegistry().releaseResources(this);
	}

	/**
	 * Tries to receive items in parameters, return items that are left after
	 * the operation.
	 */
	public ItemStack receiveItem(TileEntity tile, ItemStack stack) {
		if (currentDockingStation != null
				&& currentDockingStation.index().nextTo(new BlockIndex(tile))
				&& mainAI != null) {

			return mainAI.getActiveAI().receiveItem(stack);
		} else {
			return stack;
		}
	}

	@Override
	public int fill(ForgeDirection from, FluidStack resource, boolean doFill) {
		int result = 0;

		if (tank == null) {
			tank = new FluidStack(resource.getFluid(), 0);
		}

		if (tank.amount + resource.amount <= maxFluid) {
			result = resource.amount;

			if (doFill) {
				tank.amount += resource.amount;
			}
		} else {
			result = maxFluid - tank.amount;

			if (doFill) {
				tank.amount = maxFluid;
			}
		}

		if (tank != null && tank.amount == 0) {
			tank = null;
		}

		return result;
	}

	@Override
	public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain) {
		if (tank != null && tank.isFluidEqual(resource)) {
			return drain(from, resource.amount, doDrain);
		} else {
			return null;
		}
	}

	@Override
	public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) {
		FluidStack result = null;

		if (tank == null) {
			result = null;
		} else if (tank.amount <= maxDrain) {
			result = tank.copy();

			if (doDrain) {
				tank = null;
			}
		} else {
			result = tank.copy();
			result.amount = maxDrain;

			if (doDrain) {
				tank.amount -= maxDrain;
			}
		}

		if (tank != null && tank.amount == 0) {
			tank = null;
		}

		return result;
	}

	@Override
	public boolean canFill(ForgeDirection from, Fluid fluid) {
		return tank == null
				|| tank.amount == 0
				|| (tank.amount < maxFluid
				&& tank.getFluid().getID() == fluid.getID());
	}

	@Override
	public boolean canDrain(ForgeDirection from, Fluid fluid) {
		return tank != null
				&& tank.amount != 0
				&& tank.getFluid().getID() == fluid.getID();
	}

	@Override
	public FluidTankInfo[] getTankInfo(ForgeDirection from) {
		return new FluidTankInfo[] {new FluidTankInfo(tank, maxFluid)};
	}

    @SideOnly(Side.CLIENT)
    public IIcon getItemIcon(ItemStack stack, int renderPass) {
        IIcon iicon = super.getItemIcon(stack, renderPass);

        if (iicon == null) {
            iicon = stack.getItem().getIcon(stack, renderPass, null, itemInUse, 0);
        }

        return iicon;
    }

}
