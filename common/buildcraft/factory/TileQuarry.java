/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.factory;

import io.netty.buffer.ByteBuf;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.EnumFaceDirection;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.Ticket;
import net.minecraftforge.common.ForgeChunkManager.Type;
import buildcraft.BuildCraftFactory;
import buildcraft.api.core.BuildCraftAPI;
import buildcraft.api.core.IAreaProvider;
import buildcraft.api.core.SafeTimeTracker;
import buildcraft.api.filler.FillerManager;
import buildcraft.api.tiles.IHasWork;
import buildcraft.api.transport.IPipeConnection;
import buildcraft.api.transport.IPipeTile;
import buildcraft.api.transport.IPipeTile.PipeType;
import buildcraft.core.BlockBuildCraft;
import buildcraft.core.Box;
import buildcraft.core.Box.Kind;
import buildcraft.core.CoreConstants;
import buildcraft.core.DefaultAreaProvider;
import buildcraft.core.IDropControlInventory;
import buildcraft.core.blueprints.Blueprint;
import buildcraft.core.blueprints.BptBuilderBase;
import buildcraft.core.blueprints.BptBuilderBlueprint;
import buildcraft.core.builders.TileAbstractBuilder;
import buildcraft.core.builders.patterns.FillerPattern;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.core.utils.BlockUtils;
import buildcraft.core.utils.Utils;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class TileQuarry extends TileAbstractBuilder implements IHasWork, ISidedInventory, IDropControlInventory, IPipeConnection {

	private static enum Stage {
		BUILDING,
		DIGGING,
		MOVING,
		IDLE,
		DONE
	}

	public EntityMechanicalArm arm;
	public EntityPlayer placedBy;

	protected Box box = new Box();
	private BlockPos targetPos;
	private double headPosX, headPosY, headPosZ;
	private double speed = 0.03;
	private Stage stage = Stage.BUILDING;
	private boolean movingHorizontally;
	private boolean movingVertically;
	private double headTrajectory;

	private SafeTimeTracker updateTracker = new SafeTimeTracker(10);

	private BptBuilderBase builder;

	private final LinkedList<BlockPos> visitList = Lists.newLinkedList();

	private boolean loadDefaultBoundaries = false;
	private Ticket chunkTicket;

	private boolean frameProducer = true;

	private NBTTagCompound initNBT = null;

	private BlockMiner miner;

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
					setHead(box.xMin + 1, pos.getY() + 2, box.zMin + 1);
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
				pos.getY() + box.sizeY () - 1 + CoreConstants.PIPE_MIN_POS,
				box.zMin + CoreConstants.PIPE_MAX_POS,
				box.sizeX () - 2 + CoreConstants.PIPE_MIN_POS * 2,
				box.sizeZ() - 2 + CoreConstants.PIPE_MIN_POS * 2,
				this));
	}

	// Callback from the arm once it's created
	public void setArm(EntityMechanicalArm arm) {
		this.arm = arm;
	}

	public boolean areChunksLoaded() {
		if (BuildCraftFactory.quarryLoadsChunks) {
			// Small optimization
			return true;
		}

		// Each chunk covers the full height, so we only check one of them per height.
		return worldObj.isBlockLoaded(new BlockPos(box.xMin, box.yMax, box.zMin))
				&& worldObj.isBlockLoaded(new BlockPos(box.xMax, box.yMax, box.zMin))
				&& worldObj.isBlockLoaded(new BlockPos(box.xMin, box.yMax, box.zMax))
				&& worldObj.isBlockLoaded(new BlockPos(box.xMax, box.yMax, box.zMax));
	}
	@Override
	public void update() {
		super.update();

		if (worldObj.isRemote) {
			if (stage != Stage.DONE) {
				moveHead(speed);
			}

			return;
		}

		if (stage == Stage.DONE) {
			return;
		}

		if (!areChunksLoaded()) {
			return;
		}

		if (stage == Stage.BUILDING) {
			if (builder != null && !builder.isDone(this)) {
				builder.buildNextSlot(worldObj, this, pos);
			} else {
				stage = Stage.IDLE;
			}
		} else if (stage == Stage.DIGGING) {
			dig();
		} else if (stage == Stage.IDLE) {
			idling();
		} else if (stage == Stage.MOVING) {
			int energyToUse = 20 + (int) Math.ceil(getBattery().getEnergyStored() / 500);

			if (this.consumeEnergy(energyToUse)) {
				speed = 0.1 + energyToUse / 2000F;

				// If it's raining or snowing above the head, slow down.
				if (worldObj.isRaining()) {
					int headBPX = (int) Math.floor(headPosX);
					int headBPY = (int) Math.floor(headPosY);
					int headBPZ = (int) Math.floor(headPosZ);
					if (worldObj.getChunksLowestHorizon(headBPX, headBPZ) < headBPY) {
						speed *= 0.675;
					}
				}

				moveHead(speed);
			}
		}

		createUtilsIfNeeded();

		if (updateTracker.markTimeIfDelay(worldObj)) {
			sendNetworkUpdate();
		}
	}

	protected void dig() {
		if (worldObj.isRemote) {
			return;
		}

		if (miner == null) {
			// Hmm.
			stage = Stage.IDLE;
			return;
		}

		int rfTaken = miner.acceptEnergy(getBattery().getEnergyStored());
		getBattery().useEnergy(rfTaken, rfTaken, false);

		if (miner.hasMined()) {
			// Collect any lost items laying around
			double[] head = getHead();
			AxisAlignedBB axis = new AxisAlignedBB(head[0] - 2, head[1] - 2, head[2] - 2, head[0] + 3, head[1] + 3, head[2] + 3);
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
					miner.mineStack(mineable);
				}
			}

			stage = Stage.IDLE;
			miner = null;
		}
	}
	protected void idling() {
		if (!findTarget(true)) {
			// I believe the issue is box going null becuase of bad chunkloader positioning
			if (arm != null && box != null) {
				setTarget(new BlockPos(box.xMin + 1, pos.getY() + 2, box.zMin + 1));
			}

			stage = Stage.DONE;
		} else {
			stage = Stage.MOVING;
		}

		movingHorizontally = true;
		movingVertically = true;
		double[] head = getHead();
		BlockPos target = getTarget();
		headTrajectory = Math.atan2(target.getZ() - head[2], target.getX() - head[0]);
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

		BlockPos nextTarget = visitList.removeFirst();

		if (!columnVisitListIsUpdated) { // nextTarget may not be accurate, at least search the target column for changes
			for (int y = nextTarget.getY() + 1; y < pos.getY() + 3; y++) {
				BlockPos nextTargetPos = new BlockPos(nextTarget.getX(), y, nextTarget.getZ());
				Block block = worldObj.getBlockState(nextTargetPos).getBlock();
				if (BlockUtils.isAnObstructingBlock(block, worldObj, nextTargetPos)
						|| !BuildCraftAPI.isSoftBlock(worldObj, nextTargetPos)) {
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

		setTarget(nextTarget.offsetUp());

		return true;
	}

	/**
	 * Make the column visit list: called once per layer
	 */
	private void createColumnVisitList() {
		visitList.clear();

		Integer[][] columnHeights = new Integer[builder.blueprint.sizeX - 2][builder.blueprint.sizeZ - 2];
		boolean[][] blockedColumns = new boolean[builder.blueprint.sizeX - 2][builder.blueprint.sizeZ - 2];

		for (int searchY = pos.getY() + 3; searchY >= 0; --searchY) {
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
							columnHeights[searchX][searchZ] = height = worldObj.getChunksLowestHorizon(bx, bz);
						}

						if (height > 0 && height < by && worldObj.provider.getDimensionId() != -1) {
							continue;
						}
						BlockPos bPos = new BlockPos(bx, by, bz);
						Block block = worldObj.getBlockState(bPos).getBlock();

						if (!BlockUtils.canChangeBlock(block, worldObj, bPos)) {
							blockedColumns[searchX][searchZ] = true;
						} else if (!BuildCraftAPI.isSoftBlock(worldObj, bPos)) {
							visitList.add(bPos);
						}

						if (height == 0 && !worldObj.isAirBlock(bPos)) {
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

			box.initialize(xMin, pos.getY(), zMin, xMin + xSize - 1, pos.getY() + ySize - 1, zMin + zSize - 1);

			loadDefaultBoundaries = false;
		} else {
			// This is a legacy save, compute boundaries

			loadDefaultBoundaries = true;
		}
		
		targetPos = new BlockPos(nbttagcompound.getInteger("targetX"), nbttagcompound.getInteger("targetY"), nbttagcompound.getInteger("targetZ"));
		headPosX = nbttagcompound.getDouble("headPosX");
		headPosY = nbttagcompound.getDouble("headPosY");
		headPosZ = nbttagcompound.getDouble("headPosZ");

		// The rest of load has to be done upon initialize.
		initNBT = (NBTTagCompound) nbttagcompound.getCompoundTag("bpt").copy();
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		super.writeToNBT(nbttagcompound);

		nbttagcompound.setInteger("targetX", targetPos.getX());
		nbttagcompound.setInteger("targetY", targetPos.getY());
		nbttagcompound.setInteger("targetZ", targetPos.getZ());
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

		if (isQuarriableBlock(targetPos.offsetDown())) {
			miner = new BlockMiner(worldObj, this, targetPos.offsetDown());
			stage = Stage.DIGGING;
		} else {
			stage = Stage.IDLE;
		}
	}

	private boolean isQuarriableBlock(BlockPos bPos) {
		Block block = worldObj.getBlockState(bPos).getBlock();
		return BlockUtils.canChangeBlock(block, worldObj, bPos)
				&& !BuildCraftAPI.isSoftBlock(worldObj, bPos);
	}

	@Override
	public void invalidate() {
		if (chunkTicket != null) {
			ForgeChunkManager.releaseTicket(chunkTicket);
		}

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

		if (miner != null) {
			miner.invalidate();
		}
	}

	@Override
	public boolean hasWork() {
		return stage != Stage.DONE;
	}

	private void setBoundaries(boolean useDefaultI) {
		boolean useDefault = useDefaultI;

		if (BuildCraftFactory.quarryLoadsChunks && chunkTicket == null) {
			chunkTicket = ForgeChunkManager.requestTicket(BuildCraftFactory.instance, worldObj, Type.NORMAL);
		}
		if (chunkTicket != null) {
			chunkTicket.getModData().setInteger("quarryX", pos.getX());
			chunkTicket.getModData().setInteger("quarryY", pos.getY());
			chunkTicket.getModData().setInteger("quarryZ", pos.getZ());
			ForgeChunkManager.forceChunk(chunkTicket, new ChunkCoordIntPair(pos.getX() >> 4, pos.getZ() >> 4));
		}

		IAreaProvider a = null;

		if (!useDefault) {
			a = Utils.getNearbyAreaProvider(worldObj, pos);
		}

		if (a == null) {
			a = new DefaultAreaProvider(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 10, pos.getY() + 4, pos.getZ() + 10);

			useDefault = true;
		}

		int xSize = a.xMax() - a.xMin() + 1;
		int zSize = a.zMax() - a.zMin() + 1;

		if (chunkTicket != null) {
			if (xSize < 3 || zSize < 3 || ((xSize * zSize) >> 8) >= chunkTicket.getMaxChunkListDepth()) {
				if (placedBy != null) {
					placedBy.addChatMessage(new ChatComponentText(
							String.format(
									"Quarry size is outside of chunkloading bounds or too small %d %d (%d)",
									xSize, zSize,
									chunkTicket.getMaxChunkListDepth())));
				}

				a = new DefaultAreaProvider(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 10, pos.getY() + 4, pos.getZ() + 10);
				useDefault = true;
			}
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

			EnumFacing o = ((EnumFacing)worldObj.getBlockState(pos).getValue(BlockBuildCraft.FACING_PROP)).getOpposite();

			switch (o) {
			case EAST:
				xMin = pos.getX() + 1;
				zMin = pos.getZ() - 4 - 1;
				break;
			case WEST:
				xMin = pos.getX() - 9 - 2;
				zMin = pos.getZ() - 4 - 1;
				break;
			case SOUTH:
				xMin = pos.getX() - 4 - 1;
				zMin = pos.getZ() + 1;
				break;
			case NORTH:
			default:
				xMin = pos.getX() - 4 - 1;
				zMin = pos.getZ() - 9 - 2;
				break;
			}

			box.initialize(xMin, pos.getX(), zMin, xMin + xSize - 1, pos.getX() + ySize - 1, zMin + zSize - 1);
		}

		a.removeFromWorld();
		if (chunkTicket != null) {
			forceChunkLoading(chunkTicket);
		}

		sendNetworkUpdate();
	}

	private void initializeBlueprintBuilder() {
		Blueprint bpt = ((FillerPattern) FillerManager.registry.getPattern("buildcraft:frame"))
				.getBlueprint(box, worldObj, BuildCraftFactory.frameBlock.getDefaultState());

		builder = new BptBuilderBlueprint(bpt, worldObj, new BlockPos(box.xMin, pos.getY(), box.zMin));
		stage = Stage.BUILDING;
	}

	@Override
	public void writeData(ByteBuf stream) {
		super.writeData(stream);
		box.writeData(stream);
		Utils.writeBlockPos(stream, targetPos);
		stream.writeDouble(headPosX);
		stream.writeDouble(headPosY);
		stream.writeDouble(headPosZ);
		stream.writeFloat((float) speed);
		stream.writeFloat((float) headTrajectory);
		int flags = stage.ordinal();
		flags |= movingHorizontally ? 0x10 : 0;
		flags |= movingVertically ? 0x20 : 0;
		stream.writeByte(flags);
	}

	@Override
	public void readData(ByteBuf stream) {
		super.readData(stream);
		box.readData(stream);
		targetPos = Utils.readBlockPos(stream);
		headPosX = stream.readDouble();
		headPosY = stream.readDouble();
		headPosZ = stream.readDouble();
		speed = stream.readFloat();
		headTrajectory = stream.readFloat();
		int flags = stream.readUnsignedByte();
		stage = Stage.values()[flags & 0x07];
		movingHorizontally = (flags & 0x10) != 0;
		movingVertically = (flags & 0x20) != 0;

		createUtilsIfNeeded();

		if (arm != null) {
			arm.setHead(headPosX, headPosY, headPosZ);
			arm.updatePosition();
		}
	}

	@Override
	public void initialize() {
		super.initialize();

		if (!this.getWorld().isRemote && !box.initialized) {
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
	public String getName() {
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
	public void openInventory(EntityPlayer player) {
	}

	@Override
	public void closeInventory(EntityPlayer player) {
	}

	@Override
	public boolean isBuildingMaterialSlot(int i) {
		return true;
	}
	
	public void moveHead(double instantSpeed) {
		BlockPos target = getTarget();
		double[] head = getHead();

		if (movingHorizontally) {
			if (Math.abs(pos.getX() - head[0]) < instantSpeed * 2 && Math.abs(target.getZ() - head[2]) < instantSpeed * 2) {
				head[0] = target.getX();
				head[2] = target.getZ();

				movingHorizontally = false;

				if (!movingVertically) {
					positionReached();
					head[1] = target.getY();
				}
			} else {
				head[0] += Math.cos(headTrajectory) * instantSpeed;
				head[2] += Math.sin(headTrajectory) * instantSpeed;
			}
			setHead(head[0], head[1], head[2]);
		}

		if (movingVertically) {
			if (Math.abs(target.getY() - head[1]) < instantSpeed * 2) {
				head[1] = target.getY();

				movingVertically = false;
				if (!movingHorizontally) {
					positionReached();
					head[0] = target.getX();
					head[2] = target.getZ();
				}
			} else {
				if (target.getY() > head[1]) {
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

	private BlockPos getTarget() {
		return targetPos;
	}

	private void setTarget(BlockPos target) {
		this.targetPos = target;
	}

	public void forceChunkLoading(Ticket ticket) {
		if (chunkTicket == null) {
			chunkTicket = ticket;
		}

		Set<ChunkCoordIntPair> chunks = Sets.newHashSet();
		ChunkCoordIntPair quarryChunk = new ChunkCoordIntPair(pos.getX() >> 4, pos.getZ() >> 4);
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
							pos.getX(), pos.getY(), pos.getZ(), chunks.size())));
		}
	}

	@Override
	public boolean hasCustomName() {
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

	@Override
	public boolean doDrop() {
		return false;
	}

	@Override
	public ConnectOverride overridePipeConnection(PipeType type, EnumFacing with) {
		return type == IPipeTile.PipeType.ITEM ? ConnectOverride.CONNECT : ConnectOverride.DEFAULT;
	}

	@Override
	public int[] getSlotsForFace(EnumFacing side) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean canInsertItem(int index, ItemStack itemStackIn,
			EnumFacing direction) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean canExtractItem(int index, ItemStack stack,
			EnumFacing direction) {
		// TODO Auto-generated method stub
		return false;
	}
}
