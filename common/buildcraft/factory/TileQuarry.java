/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.factory;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet3Chat;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.Ticket;
import net.minecraftforge.common.ForgeChunkManager.Type;
import net.minecraftforge.common.ForgeDirection;
import buildcraft.BuildCraftCore;
import buildcraft.BuildCraftFactory;
import buildcraft.api.core.IAreaProvider;
import buildcraft.api.core.LaserKind;
import buildcraft.api.power.IPowerProvider;
import buildcraft.api.power.IPowerReceptor;
import buildcraft.api.power.PowerFramework;
import buildcraft.api.transport.IPipeConnection;
import buildcraft.core.Box;
import buildcraft.core.DefaultAreaProvider;
import buildcraft.core.EntityRobot;
import buildcraft.core.IBuilderInventory;
import buildcraft.core.IMachine;
import buildcraft.core.blueprints.BptBlueprint;
import buildcraft.core.blueprints.BptBuilderBase;
import buildcraft.core.blueprints.BptBuilderBlueprint;
import buildcraft.core.network.PacketUpdate;
import buildcraft.core.network.TileNetworkData;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.core.utils.BlockUtil;
import buildcraft.core.utils.Utils;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;

public class TileQuarry extends TileMachine implements IMachine, IPowerReceptor, IPipeConnection, IBuilderInventory {
	public @TileNetworkData
	Box box = new Box();
	public @TileNetworkData
	boolean inProcess = false;
	public @TileNetworkData
	int targetX, targetY, targetZ;
	public @TileNetworkData
	double headPosX, headPosY, headPosZ;
	public @TileNetworkData
	double speed = 0.03;
	public @TileNetworkData
	boolean builderDone = false;

	public EntityRobot builder;
	BptBuilderBase bluePrintBuilder;

	public EntityMechanicalArm arm;

	public IPowerProvider powerProvider;

	boolean isDigging = false;

	public static final int MAX_ENERGY = 15000;

	public TileQuarry() {
		powerProvider = PowerFramework.currentFramework.createPowerProvider();
		powerProvider.configure(20, 25, 100, 25, MAX_ENERGY);
	}

	public void createUtilsIfNeeded() {
		if (bluePrintBuilder == null) {

			if (!box.isInitialized()) {
				setBoundaries(loadDefaultBoundaries);
			}

			initializeBluePrintBuilder();
		}

		if (builderDone) {

			box.deleteLasers();

			if (arm == null) {
				createArm();
			}

			if (findTarget(false)) {
				isDigging = true;
				if (box != null && ((headPosX < box.xMin || headPosX > box.xMax) || (headPosZ < box.zMin || headPosZ > box.zMax))) {
					setHead(box.xMin + 1, yCoord + 2, box.zMin + 1);
				}
			}

		} else {

			box.createLasers(worldObj, LaserKind.Stripes);
			isDigging = true;
		}
	}

	private boolean loadDefaultBoundaries = false;
	private boolean movingHorizontally;
	private boolean movingVertically;
	private double headTrajectory;
	private Ticket chunkTicket;
	public @TileNetworkData
	boolean isAlive;
	public EntityPlayer placedBy;

	private void createArm() {

		worldObj.spawnEntityInWorld(new EntityMechanicalArm(worldObj, box.xMin + Utils.pipeMaxPos, yCoord + bluePrintBuilder.bluePrint.sizeY - 1
				+ Utils.pipeMinPos, box.zMin + Utils.pipeMaxPos, bluePrintBuilder.bluePrint.sizeX - 2 + Utils.pipeMinPos * 2, bluePrintBuilder.bluePrint.sizeZ
				- 2 + Utils.pipeMinPos * 2, this));
	}

	// Callback from the arm once it's created
	public void setArm(EntityMechanicalArm arm) {
		this.arm = arm;
	}

	@Override
	public void updateEntity() {
		if (!isAlive && CoreProxy.proxy.isSimulating(worldObj)) {
			super.updateEntity();
			return;
		}
		if (!CoreProxy.proxy.isSimulating(worldObj) && isAlive) {
			super.updateEntity();
			return;
		}
		super.updateEntity();
		if (inProcess) {
			float energyToUse = 2 + powerProvider.getEnergyStored() / 500;

			float energy = powerProvider.useEnergy(energyToUse, energyToUse, true);

			if (energy > 0) {
				moveHead(0.1 + energy / 200F);
			}
		}

		if (CoreProxy.proxy.isSimulating(worldObj) && inProcess) {
			sendNetworkUpdate();
		}
		if (inProcess || !isDigging)
			return;

		createUtilsIfNeeded();

		if (bluePrintBuilder != null) {

			builderDone = bluePrintBuilder.done;
			if (!builderDone) {

				buildFrame();
				return;

			} else {

				if (builder != null && builder.done()) {

					box.deleteLasers();
					builder.setDead();
					builder = null;
				}
			}
		}

		if (builder == null) {
			dig();
		}

	}

	@Override
	public void doWork() {
	}

	protected void buildFrame() {

		powerProvider.configure(20, 25, 100, 25, MAX_ENERGY);
		if (powerProvider.useEnergy(25, 25, true) != 25)
			return;

		powerProvider.getTimeTracker().markTime(worldObj);

		if (builder == null) {
			builder = new EntityRobot(worldObj, box);
			worldObj.spawnEntityInWorld(builder);
		}

		if (builder.readyToBuild()) {
			builder.scheduleContruction(bluePrintBuilder.getNextBlock(worldObj, this), bluePrintBuilder.getContext());
		}
	}

	protected void dig() {
		powerProvider.configure(20, 30, 500, 50, MAX_ENERGY);
		if (powerProvider.useEnergy(60, 60, true) != 60)
			return;

		if (!findTarget(true)) {

			// I believe the issue is box going null becuase of bad chunkloader positioning
			if (arm != null && box != null) {
				setTarget(box.xMin + 1, yCoord + 2, box.zMin + 1);
			}

			isDigging = false;
		}

		inProcess = true;
		movingHorizontally = true;
		movingVertically = true;
		double[] head = getHead();
		int[] target = getTarget();
		headTrajectory = Math.atan2(target[2] - head[2], target[0] - head[0]);
	}

	private final LinkedList<int[]> visitList = Lists.newLinkedList();

	public boolean findTarget(boolean doSet) {
		if (worldObj.isRemote)
			return false;

		boolean columnVisitListIsUpdated = false;

		if (visitList.isEmpty()) {
			createColumnVisitList();
			columnVisitListIsUpdated = true;
		}

		if (!doSet)
			return !visitList.isEmpty();

		if (visitList.isEmpty())
			return false;

		int[] nextTarget = visitList.removeFirst();

		if (!columnVisitListIsUpdated) { // nextTarget may not be accurate, at least search the target column for changes
			for (int y = nextTarget[1] + 1; y < yCoord + 3; y++) {
				int blockID = worldObj.getBlockId(nextTarget[0], y, nextTarget[2]);

				if (BlockUtil.canChangeBlock(blockID, worldObj, nextTarget[0], y, nextTarget[2]) && !BlockUtil.isSoftBlock(blockID, worldObj, nextTarget[0], y, nextTarget[2])) {
					createColumnVisitList();
					nextTarget = visitList.removeFirst();

					break;
				}
			}
		}

		setTarget(nextTarget[0], nextTarget[1] + 1, nextTarget[2]);

		return true;
	}

	/**
	 * Make the column visit list: called once per layer
	 */
	private void createColumnVisitList() {
		visitList.clear();

		Integer[][] columnHeights = new Integer[bluePrintBuilder.bluePrint.sizeX - 2][bluePrintBuilder.bluePrint.sizeZ - 2];
		boolean[][] blockedColumns = new boolean[bluePrintBuilder.bluePrint.sizeX - 2][bluePrintBuilder.bluePrint.sizeZ - 2];
		for (int searchY = yCoord + 3; searchY >= 0; --searchY) {
			int startX, endX, incX;

			if (searchY % 2 == 0) {
				startX = 0;
				endX = bluePrintBuilder.bluePrint.sizeX - 2;
				incX = 1;
			} else {
				startX = bluePrintBuilder.bluePrint.sizeX - 3;
				endX = -1;
				incX = -1;
			}

			for (int searchX = startX; searchX != endX; searchX += incX) {
				int startZ, endZ, incZ;

				if (searchX % 2 == searchY % 2) {
					startZ = 0;
					endZ = bluePrintBuilder.bluePrint.sizeZ - 2;
					incZ = 1;
				} else {
					startZ = bluePrintBuilder.bluePrint.sizeZ - 3;
					endZ = -1;
					incZ = -1;
				}

				for (int searchZ = startZ; searchZ != endZ; searchZ += incZ) {
					if (!blockedColumns[searchX][searchZ]) {
						Integer height = columnHeights[searchX][searchZ];
						int bx = box.xMin + searchX + 1, by = searchY, bz = box.zMin + searchZ + 1;

						if (height == null)
							columnHeights[searchX][searchZ] = height = worldObj.getHeightValue(bx, bz);

						if (height < by)
							continue;

						int blockID = worldObj.getBlockId(bx, by, bz);

						if (!BlockUtil.canChangeBlock(blockID, worldObj, bx, by, bz)) {
							blockedColumns[searchX][searchZ] = true;
						} else if (!BlockUtil.isSoftBlock(blockID, worldObj, bx, by, bz)) {
							visitList.add(new int[] { bx, by, bz });
						}
						// Stop at two planes - generally any obstructions will have been found and will force a recompute prior to this
						if (visitList.size() > bluePrintBuilder.bluePrint.sizeZ * bluePrintBuilder.bluePrint.sizeX * 2)
							return;
					}
				}
			}
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		super.readFromNBT(nbttagcompound);

		PowerFramework.currentFramework.loadPowerProvider(this, nbttagcompound);

		if (nbttagcompound.hasKey("box")) {
			box.initialize(nbttagcompound.getCompoundTag("box"));

			loadDefaultBoundaries = false;
		} else if (nbttagcompound.hasKey("xSize")) {
			// This is a legacy save, get old data

			int xMin = nbttagcompound.getInteger("xMin");
			int zMin = nbttagcompound.getInteger("zMin");

			int xSize = nbttagcompound.getInteger("xSize");
			int ySize = nbttagcompound.getInteger("ySize");
			int zSize = nbttagcompound.getInteger("zSize");

			box.initialize(xMin, yCoord, zMin, xMin + xSize - 1, yCoord + ySize - 1, zMin + zSize - 1);

			loadDefaultBoundaries = false;
		} else {
			// This is a legacy save, compute boundaries

			loadDefaultBoundaries = true;
		}

		targetX = nbttagcompound.getInteger("targetX");
		targetY = nbttagcompound.getInteger("targetY");
		targetZ = nbttagcompound.getInteger("targetZ");
		headPosX = nbttagcompound.getDouble("headPosX");
		headPosY = nbttagcompound.getDouble("headPosY");
		headPosZ = nbttagcompound.getDouble("headPosZ");
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		super.writeToNBT(nbttagcompound);

		PowerFramework.currentFramework.savePowerProvider(this, nbttagcompound);

		nbttagcompound.setInteger("targetX", targetX);
		nbttagcompound.setInteger("targetY", targetY);
		nbttagcompound.setInteger("targetZ", targetZ);
		nbttagcompound.setDouble("headPosX", headPosX);
		nbttagcompound.setDouble("headPosY", headPosY);
		nbttagcompound.setDouble("headPosZ", headPosZ);

		NBTTagCompound boxTag = new NBTTagCompound();
		box.writeToNBT(boxTag);
		nbttagcompound.setTag("box", boxTag);
	}

	@SuppressWarnings("rawtypes")
	public void positionReached() {
		inProcess = false;

		if (worldObj.isRemote)
			return;

		int i = targetX;
		int j = targetY - 1;
		int k = targetZ;

		int blockId = worldObj.getBlockId(i, j, k);

		if (isQuarriableBlock(i, j, k)) {
			powerProvider.getTimeTracker().markTime(worldObj);

			// Share this with mining well!

			List<ItemStack> stacks = BlockUtil.getItemStackFromBlock(worldObj, i, j, k);

			if (stacks != null) {
				for (ItemStack s : stacks) {
					if (s != null) {
						mineStack(s);
					}
				}
			}

			worldObj.playAuxSFXAtEntity(null, 2001, i, j, k, blockId + (worldObj.getBlockMetadata(i, j, k) << 12));
			worldObj.setBlock(i, j, k, 0);
		}

		// Collect any lost items laying around
		double[] head = getHead();
		AxisAlignedBB axis = AxisAlignedBB.getBoundingBox(head[0] - 2, head[1] - 2, head[2] - 2, head[0] + 3, head[1] + 3, head[2] + 3);
		List result = worldObj.getEntitiesWithinAABB(EntityItem.class, axis);
		for (int ii = 0; ii < result.size(); ii++) {
			if (result.get(ii) instanceof EntityItem) {
				EntityItem entity = (EntityItem) result.get(ii);
				if (entity.isDead) {
					continue;
				}

				ItemStack mineable = entity.getEntityItem();
				if (mineable.stackSize <= 0) {
					continue;
				}
				CoreProxy.proxy.removeEntity(entity);
				mineStack(mineable);
			}
		}
	}

	private void mineStack(ItemStack stack) {
		// First, try to add to a nearby chest
		ItemStack added = Utils.addToRandomInventory(stack, worldObj, xCoord, yCoord, zCoord);
		stack.stackSize -= added.stackSize;

		// Second, try to add to adjacent pipes
		if (stack.stackSize > 0) {
			Utils.addToRandomPipeEntry(this, ForgeDirection.UNKNOWN, stack);
		}

		// Lastly, throw the object away
		if (stack.stackSize > 0) {
			float f = worldObj.rand.nextFloat() * 0.8F + 0.1F;
			float f1 = worldObj.rand.nextFloat() * 0.8F + 0.1F;
			float f2 = worldObj.rand.nextFloat() * 0.8F + 0.1F;

			EntityItem entityitem = new EntityItem(worldObj, xCoord + f, yCoord + f1 + 0.5F, zCoord + f2, stack);

			entityitem.lifespan = BuildCraftCore.itemLifespan;
			entityitem.delayBeforeCanPickup = 10;

			float f3 = 0.05F;
			entityitem.motionX = (float) worldObj.rand.nextGaussian() * f3;
			entityitem.motionY = (float) worldObj.rand.nextGaussian() * f3 + 1.0F;
			entityitem.motionZ = (float) worldObj.rand.nextGaussian() * f3;
			worldObj.spawnEntityInWorld(entityitem);
		}
	}

	private boolean isQuarriableBlock(int bx, int by, int bz) {
		int blockID = worldObj.getBlockId(bx, by, bz);
		return BlockUtil.canChangeBlock(blockID, worldObj, bx, by, bz) && !BlockUtil.isSoftBlock(blockID, worldObj, bx, by, bz);
	}

	@Override
	public void invalidate() {
		ForgeChunkManager.releaseTicket(chunkTicket);

		super.invalidate();
		destroy();
	}

	@Override
	public void onChunkUnload() {
		destroy();
	}

	@Override
	public void destroy() {

		if (arm != null) {
			arm.setDead();
		}

		if (builder != null) {
			builder.setDead();
		}

		box.deleteLasers();
		arm = null;
	}

	@Override
	public boolean isActive() {
		return isDigging;
	}

	private void setBoundaries(boolean useDefault) {
		if (chunkTicket == null) {
			chunkTicket = ForgeChunkManager.requestTicket(BuildCraftFactory.instance, worldObj, Type.NORMAL);
		}
		if (chunkTicket == null) {
			isAlive = false;
			if (placedBy != null && CoreProxy.proxy.isSimulating(worldObj)) {
				PacketDispatcher.sendPacketToPlayer(
						new Packet3Chat(String.format("[BUILDCRAFT] The quarry at %d, %d, %d will not work because there are no more chunkloaders available",
								xCoord, yCoord, zCoord)), (Player) placedBy);
			}
			sendNetworkUpdate();
			return;
		}
		chunkTicket.getModData().setInteger("quarryX", xCoord);
		chunkTicket.getModData().setInteger("quarryY", yCoord);
		chunkTicket.getModData().setInteger("quarryZ", zCoord);
		ForgeChunkManager.forceChunk(chunkTicket, new ChunkCoordIntPair(xCoord >> 4, zCoord >> 4));

		IAreaProvider a = null;

		if (!useDefault) {
			a = Utils.getNearbyAreaProvider(worldObj, xCoord, yCoord, zCoord);
		}

		if (a == null) {
			a = new DefaultAreaProvider(xCoord, yCoord, zCoord, xCoord + 10, yCoord + 4, zCoord + 10);

			useDefault = true;
		}

		int xSize = a.xMax() - a.xMin() + 1;
		int zSize = a.zMax() - a.zMin() + 1;

		if (xSize < 3 || zSize < 3 || ((xSize * zSize) >> 8) >= chunkTicket.getMaxChunkListDepth()) {
			if (placedBy != null) {
				PacketDispatcher.sendPacketToPlayer(
						new Packet3Chat(String.format("Quarry size is outside of chunkloading bounds or too small %d %d (%d)", xSize, zSize,
								chunkTicket.getMaxChunkListDepth())), (Player) placedBy);
			}
			a = new DefaultAreaProvider(xCoord, yCoord, zCoord, xCoord + 10, yCoord + 4, zCoord + 10);

			useDefault = true;
		}

		xSize = a.xMax() - a.xMin() + 1;
		int ySize = a.yMax() - a.yMin() + 1;
		zSize = a.zMax() - a.zMin() + 1;

		box.initialize(a);

		if (ySize < 5) {
			ySize = 5;
			box.yMax = box.yMin + ySize - 1;
		}

		if (useDefault) {
			int xMin, zMin;

			ForgeDirection o = ForgeDirection.values()[worldObj.getBlockMetadata(xCoord, yCoord, zCoord)].getOpposite();

			switch (o) {
			case EAST:
				xMin = xCoord + 1;
				zMin = zCoord - 4 - 1;
				break;
			case WEST:
				xMin = xCoord - 9 - 2;
				zMin = zCoord - 4 - 1;
				break;
			case SOUTH:
				xMin = xCoord - 4 - 1;
				zMin = zCoord + 1;
				break;
			case NORTH:
			default:
				xMin = xCoord - 4 - 1;
				zMin = zCoord - 9 - 2;
				break;
			}

			box.initialize(xMin, yCoord, zMin, xMin + xSize - 1, yCoord + ySize - 1, zMin + zSize - 1);
		}

		a.removeFromWorld();
		forceChunkLoading(chunkTicket);
	}

	private void initializeBluePrintBuilder() {
		BptBlueprint bluePrint = new BptBlueprint(box.sizeX(), box.sizeY(), box.sizeZ());

		for (int i = 0; i < bluePrint.sizeX; ++i) {
			for (int j = 0; j < bluePrint.sizeY; ++j) {
				for (int k = 0; k < bluePrint.sizeZ; ++k) {
					bluePrint.setBlockId(i, j, k, 0);
				}
			}
		}

		for (int it = 0; it < 2; it++) {
			for (int i = 0; i < bluePrint.sizeX; ++i) {
				bluePrint.setBlockId(i, it * (box.sizeY() - 1), 0, BuildCraftFactory.frameBlock.blockID);
				bluePrint.setBlockId(i, it * (box.sizeY() - 1), bluePrint.sizeZ - 1, BuildCraftFactory.frameBlock.blockID);
			}

			for (int k = 0; k < bluePrint.sizeZ; ++k) {
				bluePrint.setBlockId(0, it * (box.sizeY() - 1), k, BuildCraftFactory.frameBlock.blockID);
				bluePrint.setBlockId(bluePrint.sizeX - 1, it * (box.sizeY() - 1), k, BuildCraftFactory.frameBlock.blockID);

			}
		}

		for (int h = 1; h < box.sizeY(); ++h) {
			bluePrint.setBlockId(0, h, 0, BuildCraftFactory.frameBlock.blockID);
			bluePrint.setBlockId(0, h, bluePrint.sizeZ - 1, BuildCraftFactory.frameBlock.blockID);
			bluePrint.setBlockId(bluePrint.sizeX - 1, h, 0, BuildCraftFactory.frameBlock.blockID);
			bluePrint.setBlockId(bluePrint.sizeX - 1, h, bluePrint.sizeZ - 1, BuildCraftFactory.frameBlock.blockID);
		}

		bluePrintBuilder = new BptBuilderBlueprint(bluePrint, worldObj, box.xMin, yCoord, box.zMin);
	}

	@Override
	public void postPacketHandling(PacketUpdate packet) {
		super.postPacketHandling(packet);

		if (isAlive) {
			createUtilsIfNeeded();
		} else {
			box.deleteLasers();
			box.reset();
			return;
		}
		if (arm != null) {
			arm.setHead(headPosX, headPosY, headPosZ);
			arm.updatePosition();
		}
	}

	@Override
	public void initialize() {
		super.initialize();

		if (CoreProxy.proxy.isSimulating(this.worldObj) && !box.initialized) {
			setBoundaries(false);
		}

		createUtilsIfNeeded();

		sendNetworkUpdate();
	}

	public void reinitalize() {
		builderDone = false;
		initializeBluePrintBuilder();
		isDigging = true;
	}

	@Override
	public void setPowerProvider(IPowerProvider provider) {
		powerProvider = provider;

	}

	@Override
	public IPowerProvider getPowerProvider() {
		return powerProvider;
	}

	@Override
	public boolean manageLiquids() {
		return false;
	}

	@Override
	public boolean manageSolids() {
		return true;
	}

	@Override
	public boolean isPipeConnected(ForgeDirection with) {
		return true;
	}

	@Override
	public int getSizeInventory() {
		return 0;
	}

	@Override
	public ItemStack getStackInSlot(int i) {
		return null;
	}

	@Override
	public ItemStack decrStackSize(int i, int j) {
		return null;
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack itemstack) {

	}

	@Override
	public ItemStack getStackInSlotOnClosing(int slot) {
		return null;
	}

	@Override
	public String getInvName() {
		return "";
	}

	@Override
	public int getInventoryStackLimit() {
		return 0;
	}
	
	@Override
	public boolean isStackValidForSlot(int i, ItemStack itemstack) {
		return false;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer entityplayer) {
		return false;
	}

	@Override
	public void openChest() {
	}

	@Override
	public void closeChest() {
	}

	@Override
	public boolean isBuildingMaterial(int i) {
		return true;
	}

	@Override
	public boolean allowActions() {
		return false;
	}

	public void moveHead(double instantSpeed) {
		int[] target = getTarget();
		double[] head = getHead();

		if (movingHorizontally) {
			if (Math.abs(target[0] - head[0]) < instantSpeed * 2 && Math.abs(target[2] - head[2]) < instantSpeed * 2) {
				head[0] = target[0];
				head[2] = target[2];

				movingHorizontally = false;

				if (!movingVertically) {
					positionReached();
					head[1] = target[1];
				}
			} else {
				head[0] += Math.cos(headTrajectory) * instantSpeed;
				head[2] += Math.sin(headTrajectory) * instantSpeed;
			}
			setHead(head[0], head[1], head[2]);
		}

		if (movingVertically) {
			if (Math.abs(target[1] - head[1]) < instantSpeed * 2) {
				head[1] = target[1];

				movingVertically = false;
				if (!movingHorizontally) {
					positionReached();
					head[0] = target[0];
					head[2] = target[2];
				}
			} else {
				if (target[1] > head[1]) {
					head[1] += instantSpeed;
				} else {
					head[1] -= instantSpeed;
				}
			}
			setHead(head[0], head[1], head[2]);
		}

		updatePosition();
	}

	private void updatePosition() {
		if (arm != null && worldObj.isRemote) {
			arm.setHead(headPosX, headPosY, headPosZ);
			arm.updatePosition();
		}
	}

	private void setHead(double x, double y, double z) {
		this.headPosX = x;
		this.headPosY = y;
		this.headPosZ = z;
	}

	private double[] getHead() {
		return new double[] { headPosX, headPosY, headPosZ };
	}

	private int[] getTarget() {
		return new int[] { targetX, targetY, targetZ };
	}

	private void setTarget(int x, int y, int z) {
		this.targetX = x;
		this.targetY = y;
		this.targetZ = z;
	}

	public void forceChunkLoading(Ticket ticket) {
		if (chunkTicket == null) {
			chunkTicket = ticket;
		}

		Set<ChunkCoordIntPair> chunks = Sets.newHashSet();
		isAlive = true;
		ChunkCoordIntPair quarryChunk = new ChunkCoordIntPair(xCoord >> 4, zCoord >> 4);
		chunks.add(quarryChunk);
		ForgeChunkManager.forceChunk(ticket, quarryChunk);

		for (int chunkX = box.xMin >> 4; chunkX <= box.xMax >> 4; chunkX++) {
			for (int chunkZ = box.zMin >> 4; chunkZ <= box.zMax >> 4; chunkZ++) {
				ChunkCoordIntPair chunk = new ChunkCoordIntPair(chunkX, chunkZ);
				ForgeChunkManager.forceChunk(ticket, chunk);
				chunks.add(chunk);
			}
		}
		if (placedBy != null) {
			PacketDispatcher.sendPacketToPlayer(
					new Packet3Chat(String.format("[BUILDCRAFT] The quarry at %d %d %d will keep %d chunks loaded", xCoord, yCoord, zCoord, chunks.size())),
					(Player) placedBy);
		}
		sendNetworkUpdate();
	}

}
