/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
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

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.WorldServer;

import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.Ticket;
import net.minecraftforge.common.ForgeChunkManager.Type;
import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.BuildCraftCore;
import buildcraft.BuildCraftFactory;
import buildcraft.api.core.BuildCraftAPI;
import buildcraft.api.core.IAreaProvider;
import buildcraft.api.core.NetworkData;
import buildcraft.api.core.SafeTimeTracker;
import buildcraft.api.filler.FillerManager;
import buildcraft.api.gates.IAction;
import buildcraft.builders.TileAbstractBuilder;
import buildcraft.builders.filler.pattern.FillerPattern;
import buildcraft.core.Box;
import buildcraft.core.Box.Kind;
import buildcraft.core.CoreConstants;
import buildcraft.core.DefaultAreaProvider;
import buildcraft.core.IMachine;
import buildcraft.core.blueprints.Blueprint;
import buildcraft.core.blueprints.BptBuilderBase;
import buildcraft.core.blueprints.BptBuilderBlueprint;
import buildcraft.core.network.PacketUpdate;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.core.utils.BlockUtil;
import buildcraft.core.utils.Utils;

public class TileQuarry extends TileAbstractBuilder implements IMachine {

	private static enum Stage {
		BUILDING,
		DIGGING,
		IDLE,
		DONE
	}

	public EntityMechanicalArm arm;
	public EntityPlayer placedBy;

	@NetworkData
	protected Box box = new Box();
	@NetworkData
	private int targetX, targetY, targetZ;
	@NetworkData
	private double headPosX, headPosY, headPosZ;
	@NetworkData
	private double speed = 0.03;
	@NetworkData
	private Stage stage = Stage.BUILDING;
	@NetworkData
	private boolean isAlive;
	@NetworkData
	private boolean movingHorizontally;
	@NetworkData
	private boolean movingVertically;
	@NetworkData
	private double headTrajectory;

	private SafeTimeTracker updateTracker = new SafeTimeTracker(10);

	private BptBuilderBase builder;

	private final LinkedList<int[]> visitList = Lists.newLinkedList();

	private boolean loadDefaultBoundaries = false;
	private Ticket chunkTicket;

	private boolean frameProducer = true;

	private NBTTagCompound initNBT = null;

	public TileQuarry () {
		box.kind = Kind.STRIPES;
	}

	public void createUtilsIfNeeded() {
		if (!worldObj.isRemote) {
			if (builder == null) {
				if (!box.isInitialized()) {
					setBoundaries(loadDefaultBoundaries);
				}

				initializeBlueprintBuilder();
			}
		}

		if (stage != Stage.BUILDING) {
			box.isVisible = false;

			if (arm == null) {
				createArm();
			}

			if (findTarget(false)) {
				if (box != null && ((headPosX < box.xMin || headPosX > box.xMax) || (headPosZ < box.zMin || headPosZ > box.zMax))) {
					setHead(box.xMin + 1, yCoord + 2, box.zMin + 1);
				}
			}
		} else {
			box.isVisible = true;
		}
	}

	private void createArm() {
		worldObj.spawnEntityInWorld
		(new EntityMechanicalArm(worldObj,
				box.xMin + CoreConstants.PIPE_MAX_POS,
				yCoord + box.sizeY () - 1 + CoreConstants.PIPE_MIN_POS,
				box.zMin + CoreConstants.PIPE_MAX_POS,
				box.sizeX () - 2 + CoreConstants.PIPE_MIN_POS * 2,
				box.sizeZ() - 2 + CoreConstants.PIPE_MIN_POS * 2,
				this));
	}

	// Callback from the arm once it's created
	public void setArm(EntityMechanicalArm arm) {
		this.arm = arm;
	}

	@Override
	public void updateEntity() {
		super.updateEntity();

		if (worldObj.isRemote) {
			if (stage != Stage.DONE) {
				moveHead(speed);
			}

			return;
		}

		if (!isAlive) {
			return;
		}

		if (stage == Stage.DONE) {
			return;
		}

		double energyToUse = 2 + mjStored / 500;

		if (mjStored > energyToUse) {
			mjStored -= energyToUse;
			speed = 0.1 + energyToUse / 200F;
			moveHead(speed);
		}

		if (stage == Stage.BUILDING) {
			if (builder != null && !builder.isDone(this)) {
				if (buildTracker.markTimeIfDelay(worldObj)) {
					builder.buildNextSlot(worldObj, this, xCoord, yCoord, zCoord);
				}
			} else {
				stage = Stage.IDLE;
			}
		} else if (stage == Stage.IDLE) {
			dig();
		}

		createUtilsIfNeeded();

		if (updateTracker.markTimeIfDelay(worldObj)) {
			sendNetworkUpdate();
		}
	}

	protected void dig() {
		float mj = BuildCraftFactory.MINING_MJ_COST_PER_BLOCK * BuildCraftFactory.miningMultiplier;

		if (mjStored < mj) {
			return;
		} else {
			mjStored -= mj;
		}

		if (!findTarget(true)) {
			// I believe the issue is box going null becuase of bad chunkloader positioning
			if (arm != null && box != null) {
				setTarget(box.xMin + 1, yCoord + 2, box.zMin + 1);
			}

			stage = Stage.DONE;
		} else {
			stage = Stage.DIGGING;
		}

		movingHorizontally = true;
		movingVertically = true;
		double[] head = getHead();
		int[] target = getTarget();
		headTrajectory = Math.atan2(target[2] - head[2], target[0] - head[0]);
		sendNetworkUpdate();
	}

	public boolean findTarget(boolean doSet) {
		if (worldObj.isRemote) {
			return false;
		}

		boolean columnVisitListIsUpdated = false;

		if (visitList.isEmpty()) {
			createColumnVisitList();
			columnVisitListIsUpdated = true;
		}

		if (!doSet) {
			return !visitList.isEmpty();
		}

		if (visitList.isEmpty()) {
			return false;
		}

		int[] nextTarget = visitList.removeFirst();

		if (!columnVisitListIsUpdated) { // nextTarget may not be accurate, at least search the target column for changes
			for (int y = nextTarget[1] + 1; y < yCoord + 3; y++) {
				Block block = worldObj.getBlock(nextTarget[0], y, nextTarget[2]);
				if (BlockUtil.isAnObstructingBlock(block, worldObj, nextTarget[0], y, nextTarget[2]) || !BuildCraftAPI.isSoftBlock(block, worldObj, nextTarget[0], y, nextTarget[2])) {
					createColumnVisitList();
					columnVisitListIsUpdated = true;
					nextTarget = null;
					break;
				}
			}
		}

		if (columnVisitListIsUpdated && nextTarget == null && !visitList.isEmpty()) {
			nextTarget = visitList.removeFirst();
		} else if (columnVisitListIsUpdated && nextTarget == null) {
			return false;
		}

		setTarget(nextTarget[0], nextTarget[1] + 1, nextTarget[2]);

		return true;
	}

	/**
	 * Make the column visit list: called once per layer
	 */
	private void createColumnVisitList() {
		visitList.clear();

		Integer[][] columnHeights = new Integer[builder.blueprint.sizeX - 2][builder.blueprint.sizeZ - 2];
		boolean[][] blockedColumns = new boolean[builder.blueprint.sizeX - 2][builder.blueprint.sizeZ - 2];

		for (int searchY = yCoord + 3; searchY >= 0; --searchY) {
			int startX, endX, incX;

			if (searchY % 2 == 0) {
				startX = 0;
				endX = builder.blueprint.sizeX - 2;
				incX = 1;
			} else {
				startX = builder.blueprint.sizeX - 3;
				endX = -1;
				incX = -1;
			}

			for (int searchX = startX; searchX != endX; searchX += incX) {
				int startZ, endZ, incZ;

				if (searchX % 2 == searchY % 2) {
					startZ = 0;
					endZ = builder.blueprint.sizeZ - 2;
					incZ = 1;
				} else {
					startZ = builder.blueprint.sizeZ - 3;
					endZ = -1;
					incZ = -1;
				}

				for (int searchZ = startZ; searchZ != endZ; searchZ += incZ) {
					if (!blockedColumns[searchX][searchZ]) {
						Integer height = columnHeights[searchX][searchZ];
						int bx = box.xMin + searchX + 1, by = searchY, bz = box.zMin + searchZ + 1;

						if (height == null) {
							columnHeights[searchX][searchZ] = height = worldObj.getHeightValue(bx, bz);
						}

						if (height > 0 && height < by && worldObj.provider.dimensionId != -1) {
							continue;
						}

						Block block = worldObj.getBlock(bx, by, bz);

						if (!BlockUtil.canChangeBlock(block, worldObj, bx, by, bz)) {
							blockedColumns[searchX][searchZ] = true;
						} else if (!BuildCraftAPI.isSoftBlock(block, worldObj, bx, by, bz)) {
							visitList.add(new int[]{bx, by, bz});
						}

						if (height == 0 && !worldObj.isAirBlock(bx, by, bz)) {
							columnHeights[searchX][searchZ] = by;
						}

						// Stop at two planes - generally any obstructions will have been found and will force a recompute prior to this

						if (visitList.size() > builder.blueprint.sizeZ * builder.blueprint.sizeX * 2) {
							return;
						}
					}
				}
			}
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		super.readFromNBT(nbttagcompound);

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

		// The rest of load has to be done upon initialize.
		initNBT = (NBTTagCompound) nbttagcompound.getCompoundTag("bpt").copy();
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		super.writeToNBT(nbttagcompound);

		nbttagcompound.setInteger("targetX", targetX);
		nbttagcompound.setInteger("targetY", targetY);
		nbttagcompound.setInteger("targetZ", targetZ);
		nbttagcompound.setDouble("headPosX", headPosX);
		nbttagcompound.setDouble("headPosY", headPosY);
		nbttagcompound.setDouble("headPosZ", headPosZ);

		NBTTagCompound boxTag = new NBTTagCompound();
		box.writeToNBT(boxTag);
		nbttagcompound.setTag("box", boxTag);

		NBTTagCompound bptNBT = new NBTTagCompound();

		if (builder != null) {
			NBTTagCompound builderCpt = new NBTTagCompound();
			builder.saveBuildStateToNBT(builderCpt, this);
			bptNBT.setTag("builderState", builderCpt);
		}

		nbttagcompound.setTag("bpt", bptNBT);
	}

	@SuppressWarnings("rawtypes")
	public void positionReached() {
		if (worldObj.isRemote) {
			return;
		}

		int i = targetX;
		int j = targetY - 1;
		int k = targetZ;

		Block block = worldObj.getBlock(i, j, k);

		if (isQuarriableBlock(i, j, k)) {

			// Share this with mining well!

			List<ItemStack> stacks = BlockUtil.getItemStackFromBlock((WorldServer) worldObj, i, j, k);

			if (stacks != null) {
				for (ItemStack s : stacks) {
					if (s != null) {
						mineStack(s);
					}
				}
			}

			worldObj.playAuxSFXAtEntity(
					null,
					2001,
					i,
					j,
					k,
					Block.getIdFromBlock(block)
							+ (worldObj.getBlockMetadata(i, j, k) << 12));
			worldObj.setBlockToAir(i, j, k);
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

		stage = Stage.IDLE;
	}

	private void mineStack(ItemStack stack) {
		// First, try to add to a nearby chest
		stack.stackSize -= Utils.addToRandomInventoryAround(worldObj, xCoord, yCoord, zCoord, stack);

		// Second, try to add to adjacent pipes
		if (stack.stackSize > 0) {
			stack.stackSize -= Utils.addToRandomPipeAround(worldObj, xCoord, yCoord, zCoord, ForgeDirection.UNKNOWN, stack);
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
		Block block = worldObj.getBlock(bx, by, bz);
		return BlockUtil.canChangeBlock(block, worldObj, bx, by, bz) && !BuildCraftAPI.isSoftBlock(block, worldObj, bx, by, bz);
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

		arm = null;

		frameProducer = false;
	}

	@Override
	public boolean isActive() {
		return stage != Stage.DONE;
	}

	private void setBoundaries(boolean useDefaultI) {
		boolean useDefault = useDefaultI;

		if (chunkTicket == null) {
			chunkTicket = ForgeChunkManager.requestTicket(BuildCraftFactory.instance, worldObj, Type.NORMAL);
		}
		if (chunkTicket == null) {
			isAlive = false;

			if (placedBy != null && !worldObj.isRemote) {
				placedBy.addChatMessage(new ChatComponentText(
						String.format(
								"[BUILDCRAFT] The quarry at %d, %d, %d will not work because there are no more chunkloaders available",
								xCoord, yCoord, zCoord)));
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
				placedBy.addChatMessage(new ChatComponentText(
						String.format(
								"Quarry size is outside of chunkloading bounds or too small %d %d (%d)",
								xSize, zSize,
								chunkTicket.getMaxChunkListDepth())));
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

	private void initializeBlueprintBuilder() {
		Blueprint bpt = ((FillerPattern) FillerManager.registry.getPattern("buildcraft:frame"))
				.getBlueprint(box, worldObj, BuildCraftFactory.frameBlock);

		builder = new BptBuilderBlueprint(bpt, worldObj, box.xMin, yCoord, box.zMin);
		stage = Stage.BUILDING;
	}

	@Override
	public void postPacketHandling(PacketUpdate packet) {
		super.postPacketHandling(packet);

		if (isAlive) {
			createUtilsIfNeeded();
		} else {
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

		if (!this.getWorldObj().isRemote && !box.initialized) {
			setBoundaries(false);
		}

		createUtilsIfNeeded();

		if (initNBT != null && builder != null) {
			builder.loadBuildStateToNBT(
					initNBT.getCompoundTag("builderState"), this);
		}

		initNBT = null;

		sendNetworkUpdate();
	}

	public void reinitalize() {
		initializeBlueprintBuilder();
	}

	@Override
	public boolean manageFluids() {
		return false;
	}

	@Override
	public boolean manageSolids() {
		return true;
	}

	@Override
	public int getSizeInventory() {
		return 1;
	}

	@Override
	public ItemStack getStackInSlot(int i) {
		if (frameProducer) {
			return new ItemStack(BuildCraftFactory.frameBlock);
		} else {
			return null;
		}
	}

	@Override
	public ItemStack decrStackSize(int i, int j) {
		if (frameProducer) {
			return new ItemStack(BuildCraftFactory.frameBlock, j);
		} else {
			return null;
		}
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack itemstack) {
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int slot) {
		return null;
	}

	@Override
	public String getInventoryName() {
		return "";
	}

	@Override
	public int getInventoryStackLimit() {
		return 0;
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemstack) {
		return false;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer entityplayer) {
		return false;
	}

	@Override
	public void openInventory() {
	}

	@Override
	public void closeInventory() {
	}

	@Override
	public boolean isBuildingMaterialSlot(int i) {
		return true;
	}

	@Override
	public boolean allowAction(IAction action) {
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
		return new double[]{headPosX, headPosY, headPosZ};
	}

	private int[] getTarget() {
		return new int[]{targetX, targetY, targetZ};
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
			placedBy.addChatMessage(new ChatComponentText(
					String.format(
							"[BUILDCRAFT] The quarry at %d %d %d will keep %d chunks loaded",
							xCoord, yCoord, zCoord, chunks.size())));
		}

		sendNetworkUpdate();
	}

	@Override
	public boolean hasCustomInventoryName() {
		return false;
	}

	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		return new Box (this).extendToEncompass(box).expand(50).getBoundingBox();
	}

	@Override
	public Box getBox() {
		return box;
	}
}
