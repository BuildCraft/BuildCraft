/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.robotics;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;

import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import io.netty.buffer.ByteBuf;

import net.minecraft.client.Minecraft;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemSkull;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import net.minecraft.util.StringUtils;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import cpw.mods.fml.common.registry.IEntityAdditionalSpawnData;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

import buildcraft.BuildCraftCore;
import buildcraft.api.boards.RedstoneBoardNBT;
import buildcraft.api.boards.RedstoneBoardRegistry;
import buildcraft.api.boards.RedstoneBoardRobot;
import buildcraft.api.boards.RedstoneBoardRobotNBT;
import buildcraft.api.core.BCLog;
import buildcraft.api.core.BlockIndex;
import buildcraft.api.core.IZone;
import buildcraft.api.events.RobotEvent;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.DockingStation;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.api.robots.IRobotOverlayItem;
import buildcraft.api.robots.RobotManager;
import buildcraft.api.statements.StatementSlot;
import buildcraft.api.tiles.IDebuggable;
import buildcraft.core.DefaultProps;
import buildcraft.core.ItemWrench;
import buildcraft.core.LaserData;
import buildcraft.core.lib.RFBattery;
import buildcraft.core.lib.network.command.CommandWriter;
import buildcraft.core.lib.network.command.ICommandReceiver;
import buildcraft.core.lib.network.command.PacketCommand;
import buildcraft.core.lib.utils.NetworkUtils;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.robotics.ai.AIRobotMain;
import buildcraft.robotics.ai.AIRobotShutdown;
import buildcraft.robotics.ai.AIRobotSleep;
import buildcraft.robotics.statements.ActionRobotWorkInArea;
import buildcraft.robotics.statements.ActionRobotWorkInArea.AreaType;

public class EntityRobot extends EntityRobotBase implements
		IEntityAdditionalSpawnData, IInventory, IFluidHandler, ICommandReceiver, IDebuggable {

	public static final ResourceLocation ROBOT_BASE = new ResourceLocation(
			DefaultProps.TEXTURE_PATH_ROBOTS + "/robot_base.png");
	public static final int MAX_WEARABLES = 8;
	public static final int TRANSFER_INV_SLOTS = 4;

	private static Set<Integer> blacklistedItemsForUpdate = Sets.newHashSet();

    public LaserData laser = new LaserData();
	public DockingStation linkedDockingStation;
	public BlockIndex linkedDockingStationIndex;
	public ForgeDirection linkedDockingStationSide;

	public BlockIndex currentDockingStationIndex;
	public ForgeDirection currentDockingStationSide;

	public boolean isDocked = false;

	public RedstoneBoardRobot board;
	public AIRobotMain mainAI;

	public ItemStack itemInUse;
	public float itemAngle1 = 0;
	public float itemAngle2 = 0;
	public boolean itemActive = false;
	public float itemActiveStage = 0;
	public long lastUpdateTime = 0;

	private DockingStation currentDockingStation;
	private List<ItemStack> wearables = new ArrayList<ItemStack>();

	private boolean needsUpdate = false;
	private ItemStack[] inv = new ItemStack[TRANSFER_INV_SLOTS];
	private FluidStack tank;
	private int maxFluid = FluidContainerRegistry.BUCKET_VOLUME * 4;
	private ResourceLocation texture;

	private WeakHashMap<Entity, Long> unreachableEntities = new WeakHashMap<Entity, Long>();

	private NBTTagList stackRequestNBT;

	private RFBattery battery = new RFBattery(MAX_ENERGY, MAX_ENERGY, 100);

	private boolean firstUpdateDone = false;

	private boolean isActiveClient = false;

	private long robotId = EntityRobotBase.NULL_ROBOT_ID;

	private int energySpendPerCycle = 0;
	private int ticksCharging = 0;
	private float energyFX = 0;
	private int steamDx = 0;
	private int steamDy = -1;
	private int steamDz = 0;

	public EntityRobot(World world, RedstoneBoardRobotNBT boardNBT) {
		this(world);

		board = boardNBT.create(this);
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

		dataWatcher.addObject(12, (float) 0);
		dataWatcher.addObject(13, (float) 0);
		dataWatcher.addObject(14, (float) 0);
		dataWatcher.addObject(15, (byte) 0);
		dataWatcher.addObject(16, "");
		dataWatcher.addObject(17, (float) 0);
		dataWatcher.addObject(18, (float) 0);
		dataWatcher.addObject(19, 0);
		dataWatcher.addObject(20, (byte) 0);
		dataWatcher.addObject(21, 0);
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
		isActiveClient = dataWatcher.getWatchableObjectByte(20) == 1;
		battery.setEnergy(dataWatcher.getWatchableObjectInt(21));
	}

	protected void updateDataServer() {
		dataWatcher.updateObject(12, (float) laser.tail.x);
		dataWatcher.updateObject(13, (float) laser.tail.y);
		dataWatcher.updateObject(14, (float) laser.tail.z);
		dataWatcher.updateObject(15, (byte) (laser.isVisible ? 1 : 0));
		dataWatcher.updateObject(17, itemAngle1);
		dataWatcher.updateObject(18, itemAngle2);
	}

	public boolean isActive() {
		if (worldObj.isRemote) {
			return isActiveClient;
		} else {
			return mainAI.getActiveAI() instanceof AIRobotSleep || mainAI.getActiveAI() instanceof AIRobotShutdown;
		}
	}

	protected void init() {
		if (worldObj.isRemote) {
			BuildCraftCore.instance.sendToServer(new PacketCommand(this, "requestInitialization", null));
		}
	}

	public void setLaserDestination(float x, float y, float z) {
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
	public String getCommandSenderName() {
		return StatCollector.translateToLocal("item.robot.name");
	}

	@Override
	public void onEntityUpdate() {
		this.worldObj.theProfiler.startSection("bcEntityRobot");
		if (!firstUpdateDone) {
			firstUpdate();
			firstUpdateDone = true;
		}

		if (ticksCharging > 0) {
			ticksCharging--;
		}

		if (!worldObj.isRemote) {
			// The client-side sleep indicator should also display if the robot is charging.
			// To not break gates and other things checking for sleep, this is done here.
			dataWatcher.updateObject(20, (byte) ((isActive() && ticksCharging == 0) ? 1 : 0));
			dataWatcher.updateObject(21, getEnergy());

			if (needsUpdate) {
				updateDataServer();
				needsUpdate = false;
			}
		}

		if (worldObj.isRemote) {
			updateDataClient();
			updateRotationYaw(60.0f);
			updateEnergyFX();
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
				if (linkedDockingStationIndex != null) {
					linkedDockingStation = getRegistry().getStation(linkedDockingStationIndex.x,
							linkedDockingStationIndex.y, linkedDockingStationIndex.z,
							linkedDockingStationSide);
				}

				if (linkedDockingStation == null) {
					shutdown("no docking station");
				} else {
					if (linkedDockingStation.robotTaking() != this) {
						if (linkedDockingStation.robotIdTaking() == robotId) {
							BCLog.logger.warn("A robot entity was not properly unloaded");
							linkedDockingStation.invalidateRobotTakingEntity();
						}
						if (linkedDockingStation.robotTaking() != this) {
							shutdown("wrong docking station");
						}
					}
				}
			}

			if (currentDockingStationIndex != null && currentDockingStation == null) {
				currentDockingStation = getRegistry().getStation(
						currentDockingStationIndex.x,
						currentDockingStationIndex.y,
						currentDockingStationIndex.z,
						currentDockingStationSide);
			}

			if (posY < -128) {
				isDead = true;

				BCLog.logger.info("Destroying robot " + this.toString() + " - Fallen into Void");
				getRegistry().killRobot(this);
			}

			if (linkedDockingStation == null || linkedDockingStation.isInitialized()) {
				this.worldObj.theProfiler.startSection("bcRobotAI");
				mainAI.cycle();
				this.worldObj.theProfiler.endSection();

				if (energySpendPerCycle != mainAI.getActiveAI().getEnergyCost()) {
					energySpendPerCycle = mainAI.getActiveAI().getEnergyCost();
					dataWatcher.updateObject(19, energySpendPerCycle);
				}
			}
		}


		// tick all carried itemstacks
		for (int i = 0; i < inv.length; i++) {
			updateItem(inv[i], i, false);
		}

		// tick the item the robot is currently holding
		updateItem(itemInUse, 0, true);

		// do not tick wearables or equipment from EntityLiving


		super.onEntityUpdate();
		this.worldObj.theProfiler.endSection();
	}

	@Override
	protected void updateEntityActionState() {
	}

	@Override
	public boolean handleWaterMovement() {
		return false;
	}

	@SideOnly(Side.CLIENT)
	private void updateEnergyFX() {
		energyFX += energySpendPerCycle;

		if (energyFX >= (100 << (2 * Minecraft.getMinecraft().gameSettings.particleSetting))) {
			energyFX = 0;
			spawnEnergyFX();
		}
	}

	@SideOnly(Side.CLIENT)
	private void spawnEnergyFX() {
		Minecraft.getMinecraft().effectRenderer.addEffect(new EntityRobotEnergyParticle(
				worldObj,
				posX + steamDx * 0.25, posY + steamDy * 0.25, posZ + steamDz * 0.25,
				steamDx * 0.05, steamDy * 0.05, steamDz * 0.05,
				energySpendPerCycle * 0.075F < 1 ? 1 : energySpendPerCycle * 0.075F));
	}

	@Override
	public AxisAlignedBB getBoundingBox() {
		return AxisAlignedBB.getBoundingBox(posX - 0.25F, posY - 0.25F, posZ - 0.25F, posX + 0.25F, posY + 0.25F, posZ + 0.25F);
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

	private void shutdown(String reason) {
		if (!(mainAI.getDelegateAI() instanceof AIRobotShutdown)) {
			BCLog.logger.info("Shutting down robot " + this.toString() + " - " + reason);
			mainAI.startDelegateAI(new AIRobotShutdown(this));
		}
	}

	@Override
	public void writeSpawnData(ByteBuf data) {
		data.writeByte(wearables.size());
		for (ItemStack s : wearables) {
			NetworkUtils.writeStack(data, s);
		}
	}

	@Override
	public void readSpawnData(ByteBuf data) {
		int amount = data.readUnsignedByte();
		while (amount > 0) {
			wearables.add(NetworkUtils.readStack(data));
			amount--;
		}
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
		return new ItemStack[0];
	}

	@Override
	protected void fall(float par1) {
	}

	@Override
	protected void updateFallState(double par1, boolean par3) {
	}

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

		if (linkedDockingStationIndex != null) {
			NBTTagCompound linkedStationNBT = new NBTTagCompound();
			NBTTagCompound linkedStationIndexNBT = new NBTTagCompound();
			linkedDockingStationIndex.writeTo(linkedStationIndexNBT);
			linkedStationNBT.setTag("index", linkedStationIndexNBT);
			linkedStationNBT.setByte("side", (byte) linkedDockingStationSide.ordinal());
			nbt.setTag("linkedStation", linkedStationNBT);
		}

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

		if (wearables.size() > 0) {
			NBTTagList wearableList = new NBTTagList();

			for (ItemStack wearable : wearables) {
				NBTTagCompound item = new NBTTagCompound();
				wearable.writeToNBT(item);
				wearableList.appendTag(item);
			}

			nbt.setTag("wearables", wearableList);
		}

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

		if (nbt.hasKey("linkedStation")) {
			NBTTagCompound linkedStationNBT = nbt.getCompoundTag("linkedStation");
			linkedDockingStationIndex = new BlockIndex(linkedStationNBT.getCompoundTag("index"));
			linkedDockingStationSide = ForgeDirection.values()[linkedStationNBT.getByte("side")];
		}

		if (nbt.hasKey("currentStation")) {
			NBTTagCompound currentStationNBT = nbt.getCompoundTag("currentStation");
			currentDockingStationIndex = new BlockIndex(currentStationNBT.getCompoundTag("index"));
			currentDockingStationSide = ForgeDirection.values()[currentStationNBT.getByte("side")];

		}

		laser.readFromNBT(nbt.getCompoundTag("laser"));

		battery.readFromNBT(nbt.getCompoundTag("battery"));

		wearables.clear();
		if (nbt.hasKey("wearables")) {
			NBTTagList list = nbt.getTagList("wearables", 10);
			for (int i = 0; i < list.tagCount(); i++) {
				ItemStack stack = ItemStack.loadItemStackFromNBT(list.getCompoundTagAt(i));
				if (stack != null) {
					wearables.add(stack);
				}
			}
		}

		if (nbt.hasKey("itemInUse")) {
			itemInUse = ItemStack.loadItemStackFromNBT(nbt.getCompoundTag("itemInUse"));
			itemActive = nbt.getBoolean("itemActive");
		}

		for (int i = 0; i < inv.length; ++i) {
			inv[i] = ItemStack.loadItemStackFromNBT(nbt.getCompoundTag("inv[" + i + "]"));
		}

		NBTTagCompound ai = nbt.getCompoundTag("mainAI");
		mainAI = (AIRobotMain) AIRobot.loadAI(ai, this);

		if (nbt.hasKey("board")) {
			board = (RedstoneBoardRobot) AIRobot.loadAI(nbt.getCompoundTag("board"), this);
		} else {
			board = (RedstoneBoardRobot) mainAI.getDelegateAI();
		}

		if (board == null) {
			board = RedstoneBoardRegistry.instance.getEmptyRobotBoard().create(this);
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

		// Restore robot persistence on pre-6.1.9 robotics
		this.func_110163_bv();
	}

	@Override
	public void dock(DockingStation station) {
		currentDockingStation = station;

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

	@Override
	public void setMainStation(DockingStation station) {
		if (linkedDockingStation != null && linkedDockingStation != station) {
			linkedDockingStation.unsafeRelease(this);
		}

		linkedDockingStation = station;
		if (station != null) {
			linkedDockingStationIndex = linkedDockingStation.index();
			linkedDockingStationSide = linkedDockingStation.side();
		} else {
			linkedDockingStationIndex = null;
			linkedDockingStationSide = ForgeDirection.UNKNOWN;
		}
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
		ItemStack stack = inv[var1];
		inv[var1] = null;
		return stack;
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
		BuildCraftCore.instance.sendToEntity(new PacketCommand(this, "clientSetInventory", new CommandWriter() {
			public void write(ByteBuf data) {
				data.writeShort(slot);
				NetworkUtils.writeStack(data, inv[slot]);
			}
		}), this);
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
		BuildCraftCore.instance.sendToEntity(new PacketCommand(this, "clientSetItemInUse", new CommandWriter() {
			public void write(ByteBuf data) {
				NetworkUtils.writeStack(data, itemInUse);
			}
		}), this);
	}

	private void setSteamDirection(final int x, final int y, final int z) {
		if (!worldObj.isRemote) {
			BuildCraftCore.instance.sendToEntity(new PacketCommand(this, "setSteamDirection", new CommandWriter() {
				public void write(ByteBuf data) {
					data.writeInt(x);
					data.writeShort(y);
					data.writeInt(z);
				}
			}), this);
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
				itemInUse = NetworkUtils.readStack(stream);
			} else if ("clientSetInventory".equals(command)) {
				int slot = stream.readUnsignedShort();
				inv[slot] = NetworkUtils.readStack(stream);
			} else if ("initialize".equals(command)) {
				itemInUse = NetworkUtils.readStack(stream);
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
			} else if ("syncWearables".equals(command)) {
				wearables.clear();

				int amount = stream.readUnsignedByte();
				while (amount > 0) {
					wearables.add(NetworkUtils.readStack(stream));
					amount--;
				}
			}
		} else if (side.isServer()) {
			EntityPlayer p = (EntityPlayer) sender;
			if ("requestInitialization".equals(command)) {
				BuildCraftCore.instance.sendToPlayer(p, new PacketCommand(this, "initialize", new CommandWriter() {
					public void write(ByteBuf data) {
						NetworkUtils.writeStack(data, itemInUse);
						data.writeBoolean(itemActive);
					}
				}));

				for (int i = 0; i < inv.length; ++i) {
					final int j = i;
					BuildCraftCore.instance.sendToPlayer(p, new PacketCommand(this, "clientSetInventory", new CommandWriter() {
						public void write(ByteBuf data) {
							data.writeShort(j);
							NetworkUtils.writeStack(data, inv[j]);
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
	public boolean attackEntityFrom(DamageSource source, float f) {
		// Ignore hits from mobs or when docked.
		Entity src = source.getSourceOfDamage();
		if (src != null && !(src instanceof EntityFallingBlock) && !(src instanceof IMob) && currentDockingStation == null) {
			if (ForgeHooks.onLivingAttack(this, source, f)) {
				return false;
			}

			if (!worldObj.isRemote) {
				hurtTime = maxHurtTime = 10;

				int mul = 2600;
				for (ItemStack s : wearables) {
					if (s.getItem() instanceof ItemArmor) {
						mul = mul * 2 / (2 + ((ItemArmor) s.getItem()).damageReduceAmount);
					} else {
						mul *= 0.7;
					}
				}

				int energy = Math.round(f * mul);
				if (battery.getEnergyStored() - energy > 0) {
					battery.setEnergy(battery.getEnergyStored() - energy);
					return true;
				} else {
					onRobotHit(true);
				}
			}
			return true;
		}
		return false;
	}

	@Override
	public float getAimYaw() {
		return itemAngle1;
	}

	@Override
	public float getAimPitch() {
		return itemAngle2;
	}

	@Override
	public void aimItemAt(float yaw, float pitch) {
		itemAngle1 = yaw;
		itemAngle2 = pitch;

		updateDataServer();
	}

	@Override
	public void aimItemAt(int x, int y, int z) {
		int deltaX = x - (int) Math.floor(posX);
		int deltaY = y - (int) Math.floor(posY);
		int deltaZ = z - (int) Math.floor(posZ);

		if (deltaX != 0 || deltaZ != 0) {
			itemAngle1 = (float) (Math.atan2(deltaZ, deltaX) * 180f / Math.PI) + 180f;
		}
		double d3 = MathHelper.sqrt_double(deltaX * deltaX + deltaZ * deltaZ);
		itemAngle2 = (float) (-(Math.atan2(deltaY, d3) * 180.0D / Math.PI));

		setSteamDirection(deltaX, deltaY, deltaZ);

		updateDataServer();
	}

	private void updateRotationYaw(float maxStep) {
		float step = MathHelper.wrapAngleTo180_float(itemAngle1 - rotationYaw);

		if (step > maxStep) {
			step = maxStep;
		}

		if (step < -maxStep) {
			step = -maxStep;
		}

		rotationYaw = rotationYaw + step;
	}

	@Override
	protected float func_110146_f(float targetYaw, float dist) {
		if (worldObj.isRemote) {
			float f2 = MathHelper.wrapAngleTo180_float(this.rotationYaw - this.renderYawOffset);
			this.renderYawOffset += f2 * 0.5F;
			float f3 = MathHelper.wrapAngleTo180_float(this.rotationYaw - this.renderYawOffset);
			boolean flag = f3 < -90.0F || f3 >= 90.0F;

			this.renderYawOffset = this.rotationYaw - f3;

			if (f3 * f3 > 2500.0F) {
				this.renderYawOffset += f3 * 0.2F;
			}

			float newDist = dist;
			if (flag) {
				newDist *= -1.0F;
			}

			return newDist;
		}
		return 0;
	}

	@Override
	public void setItemActive(final boolean isActive) {
		if (isActive != itemActive) {
			itemActive = isActive;
			BuildCraftCore.instance.sendToEntity(new PacketCommand(this, "setItemActive", new CommandWriter() {
				public void write(ByteBuf data) {
					data.writeBoolean(isActive);
				}
			}), this);
		}
	}

	@Override
	public RedstoneBoardRobot getBoard() {
		return board;
	}

	@Override
	public DockingStation getLinkedStation() {
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
		if (MinecraftForge.EVENT_BUS.post(new AttackEntityEvent(
				CoreProxy.proxy.getBuildCraftPlayer((WorldServer) worldObj, (int) posX, (int) posY, (int) posZ).get(),
				par1Entity))) {
			return;
		}

		if (par1Entity.canAttackWithItem()) {
			if (!par1Entity.hitByEntity(this)) {
				Multimap<String, AttributeModifier> attributes = itemInUse != null ? (Multimap<String, AttributeModifier>) itemInUse.getAttributeModifiers() : null;
				float attackDamage = 2.0F;
				int knockback = 0;

				if (attributes != null) {
					for (AttributeModifier modifier : attributes.get(SharedMonsterAttributes.attackDamage.getAttributeUnlocalizedName())) {
						switch (modifier.getOperation()) {
							case 0:
								attackDamage += modifier.getAmount();
								break;
							case 1:
								attackDamage *= modifier.getAmount();
								break;
							case 2:
								attackDamage *= 1.0F + modifier.getAmount();
								break;
						}
					}
				}

				if (par1Entity instanceof EntityLivingBase) {
					attackDamage += EnchantmentHelper.getEnchantmentModifierLiving(this, (EntityLivingBase) par1Entity);
					knockback += EnchantmentHelper.getKnockbackModifier(this, (EntityLivingBase) par1Entity);
				}

				if (attackDamage > 0.0F) {
					int fireAspect = EnchantmentHelper.getFireAspectModifier(this);

					if (par1Entity instanceof EntityLivingBase && fireAspect > 0 && !par1Entity.isBurning()) {
						par1Entity.setFire(fireAspect * 4);
					}

					if (par1Entity.attackEntityFrom(new EntityDamageSource("robot", this), attackDamage)) {
						this.setLastAttacker(par1Entity);

						if (knockback > 0) {
							par1Entity.addVelocity((double) (-MathHelper.sin(this.rotationYaw * (float) Math.PI / 180.0F) * (float) knockback * 0.5F), 0.1D, (double) (MathHelper.cos(this.rotationYaw * (float) Math.PI / 180.0F) * (float) knockback * 0.5F));
							this.motionX *= 0.6D;
							this.motionZ *= 0.6D;
							this.setSprinting(false);
						}

						if (par1Entity instanceof EntityLivingBase) {
							EnchantmentHelper.func_151384_a((EntityLivingBase) par1Entity, this);
						}

						EnchantmentHelper.func_151385_b(this, par1Entity);

						ItemStack itemstack = itemInUse;

						if (itemstack != null && par1Entity instanceof EntityLivingBase) {
							itemstack.getItem().hitEntity(itemstack, (EntityLivingBase) par1Entity, this);
						}

						if (itemInUse.stackSize == 0) {
							setItemInUse(null);
						}
					}
				}
			}
		}
	}

	@Override
	public IZone getZoneToWork() {
		return getZone(ActionRobotWorkInArea.AreaType.WORK);
	}

	@Override
	public IZone getZoneToLoadUnload() {
		IZone zone = getZone(ActionRobotWorkInArea.AreaType.LOAD_UNLOAD);
		if (zone == null) {
			zone = getZoneToWork();
		}
		return zone;
	}

	private IZone getZone(AreaType areaType) {
		if (linkedDockingStation != null) {
			for (StatementSlot s : linkedDockingStation.getActiveActions()) {
				if (s.statement instanceof ActionRobotWorkInArea
						&& ((ActionRobotWorkInArea) s.statement).getAreaType() == areaType) {
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
		unreachableEntities.put(entity, worldObj.getTotalWorldTime() + 1200);
	}

	@Override
	public boolean isKnownUnreachable(Entity entity) {
		if (unreachableEntities.containsKey(entity)) {
			if (unreachableEntities.get(entity) >= worldObj.getTotalWorldTime()) {
				return true;
			} else {
				unreachableEntities.remove(entity);
				return false;
			}
		} else {
			return false;
		}
	}

	protected void onRobotHit(boolean attacked) {
		if (!worldObj.isRemote) {
			if (attacked) {
				convertToItems();
			} else {
				if (wearables.size() > 0) {
					entityDropItem(wearables.remove(wearables.size() - 1), 0);
					syncWearablesToClient();
				} else if (itemInUse != null) {
					entityDropItem(itemInUse, 0);
					itemInUse = null;
				} else {
					convertToItems();
				}
			}
		}
	}

	@Override
	protected boolean interact(EntityPlayer player) {
		ItemStack stack = player.getCurrentEquippedItem();
		if (stack == null || stack.getItem() == null) {
			return false;
		}

		RobotEvent.Interact robotInteractEvent = new RobotEvent.Interact(this, player, stack);
		MinecraftForge.EVENT_BUS.post(robotInteractEvent);
		if (robotInteractEvent.isCanceled()) {
			return false;
		}

		if (player.isSneaking() && stack.getItem() == BuildCraftCore.wrenchItem) {
			RobotEvent.Dismantle robotDismantleEvent = new RobotEvent.Dismantle(this, player);
			MinecraftForge.EVENT_BUS.post(robotDismantleEvent);
			if (robotDismantleEvent.isCanceled()) {
				return false;
			}

			onRobotHit(false);

			if (worldObj.isRemote) {
				((ItemWrench) stack.getItem()).wrenchUsed(player, 0, 0, 0);
			}
			return true;
		} else if (wearables.size() < MAX_WEARABLES && stack.getItem().isValidArmor(stack, 0, this)) {
			if (!worldObj.isRemote) {
				wearables.add(stack.splitStack(1));
				syncWearablesToClient();
			} else {
				player.swingItem();
			}
			return true;
		} else if (wearables.size() < MAX_WEARABLES && stack.getItem() instanceof IRobotOverlayItem && ((IRobotOverlayItem) stack.getItem()).isValidRobotOverlay(stack)) {
			if (!worldObj.isRemote) {
				wearables.add(stack.splitStack(1));
				syncWearablesToClient();
			} else {
				player.swingItem();
			}
			return true;
		} else if (wearables.size() < MAX_WEARABLES && stack.getItem() instanceof ItemSkull) {
			if (!worldObj.isRemote) {
				ItemStack skullStack = stack.splitStack(1);
				initSkullItem(skullStack);
				wearables.add(skullStack);
				syncWearablesToClient();
			} else {
				player.swingItem();
			}
			return true;
		} else {
			return super.interact(player);
		}
	}

	private void initSkullItem(ItemStack skullStack) {
		if (skullStack.hasTagCompound()) {
			NBTTagCompound nbttagcompound = skullStack.getTagCompound();
			GameProfile gameProfile = null;

			if (nbttagcompound.hasKey("SkullOwner", NBT.TAG_COMPOUND)) {
				gameProfile = NBTUtil.func_152459_a(nbttagcompound.getCompoundTag("SkullOwner"));
			} else if (nbttagcompound.hasKey("SkullOwner", NBT.TAG_STRING)
					&& !StringUtils.isNullOrEmpty(nbttagcompound.getString("SkullOwner"))) {
				gameProfile = new GameProfile(null, nbttagcompound.getString("SkullOwner"));
			}
			if (gameProfile != null && !StringUtils.isNullOrEmpty(gameProfile.getName())) {
				if (!gameProfile.isComplete()
						|| !gameProfile.getProperties().containsKey("textures")) {
					gameProfile = MinecraftServer.getServer().func_152358_ax()
							.func_152655_a(gameProfile.getName());

					if (gameProfile != null) {
						Property property = (Property) Iterables.getFirst(gameProfile
								.getProperties().get("textures"), (Object) null);

						if (property == null) {
							gameProfile = MinecraftServer.getServer().func_147130_as()
									.fillProfileProperties(gameProfile, true);
						}
					}
				}
			}
			if (gameProfile != null && gameProfile.isComplete()
					&& gameProfile.getProperties().containsKey("textures")) {
				NBTTagCompound profileNBT = new NBTTagCompound();
				NBTUtil.func_152460_a(profileNBT, gameProfile);
				nbttagcompound.setTag("SkullOwner", profileNBT);
			} else {
				nbttagcompound.removeTag("SkullOwner");
			}
		}
	}

	private void syncWearablesToClient() {
		BuildCraftCore.instance.sendToEntity(new PacketCommand(this, "syncWearables", new CommandWriter() {
			public void write(ByteBuf data) {
				data.writeByte(wearables.size());
				for (ItemStack s : wearables) {
					NetworkUtils.writeStack(data, s);
				}
			}
		}), this);
	}

	private List<ItemStack> getDrops() {
		List<ItemStack> drops = new ArrayList<ItemStack>();
		drops.add(ItemRobot.createRobotStack(board.getNBTHandler(), battery.getEnergyStored()));
		if (itemInUse != null) {
			drops.add(itemInUse);
		}
		for (ItemStack element : inv) {
			if (element != null) {
				drops.add(element);
			}
		}
		drops.addAll(wearables);
		return drops;
	}

	private void convertToItems() {
		if (!worldObj.isRemote && !isDead) {
			if (mainAI != null) {
				mainAI.abort();
			}
			List<ItemStack> drops = getDrops();
			for (ItemStack stack : drops) {
				entityDropItem(stack, 0);
			}
			isDead = true;
		}

		getRegistry().killRobot(this);
	}

	@Override
	public void setDead() {
		if (worldObj.isRemote) {
			super.setDead();
		}
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
		return (RobotRegistry) RobotManager.registryProvider.getRegistry(worldObj);
	}

	@Override
	public void releaseResources() {
		getRegistry().releaseResources(this);
	}

	/**
	 * Tries to receive items in parameters, return items that are left after
	 * the operation.
	 */
	@Override
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
		int result;

		if (tank != null && !tank.isFluidEqual(resource)) {
			return 0;
		}

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
		FluidStack result;

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
		return new FluidTankInfo[]{new FluidTankInfo(tank, maxFluid)};
	}

	@SideOnly(Side.CLIENT)
	public IIcon getItemIcon(ItemStack stack, int renderPass) {
		IIcon iicon = super.getItemIcon(stack, renderPass);

		if (iicon == null) {
			iicon = stack.getItem().getIcon(stack, renderPass, null, itemInUse, 0);
		}

		return iicon;
	}

	@Override
	public void getDebugInfo(List<String> info, ForgeDirection side, ItemStack debugger, EntityPlayer player) {
		info.add("Robot " + board.getNBTHandler().getID() + " (" + getBattery().getEnergyStored() + "/" + getBattery().getMaxEnergyStored() + " RF)");
		info.add(String.format("Position: %.2f, %.2f, %.2f", posX, posY, posZ));
		info.add("AI tree:");
		AIRobot aiRobot = mainAI;
		while (aiRobot != null) {
			info.add("- " + RobotManager.getAIRobotName(aiRobot.getClass()) + " (" + aiRobot.getEnergyCost() + " RF/t)");
			if (aiRobot instanceof IDebuggable) {
				((IDebuggable) aiRobot).getDebugInfo(info, side, debugger, player);
			}
			aiRobot = aiRobot.getDelegateAI();
		}
	}

	public int receiveEnergy(int maxReceive, boolean simulate) {
		int energyReceived = getBattery().receiveEnergy(maxReceive, simulate);

		// 5 RF/t is set as the "sleep threshold" for detecting charging.
		if (!simulate && energyReceived > 5 && ticksCharging <= 25) {
			ticksCharging += 5;
		}

		return energyReceived;
	}

	public List<ItemStack> getWearables() {
		return wearables;
	}

	private void updateItem(ItemStack stack, int i, boolean held) {
		if (stack != null && stack.getItem() != null) {
			int id = Item.getIdFromItem(stack.getItem());
			// did this item not throw an exception before?
			if (!blacklistedItemsForUpdate.contains(id)) {
				try {
					stack.getItem().onUpdate(stack, worldObj, this, i, held);
				} catch (Exception e) {
					// the item threw an exception, print it and do not let it update once more
					e.printStackTrace();
					blacklistedItemsForUpdate.add(id);
				}
			}
		}
	}
}
