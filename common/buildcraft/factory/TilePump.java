/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.factory;

import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeMap;

import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import buildcraft.BuildCraftCore;
import buildcraft.BuildCraftFactory;
import net.minecraft.util.BlockPos;
import buildcraft.api.core.SafeTimeTracker;
import buildcraft.api.tiles.IHasWork;
import buildcraft.core.CoreConstants;
import buildcraft.core.EntityBlock;
import buildcraft.core.RFBattery;
import buildcraft.core.TileBuffer;
import buildcraft.core.TileBuildCraft;
import buildcraft.core.fluids.SingleUseTank;
import buildcraft.core.fluids.TankUtils;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.core.utils.BlockUtils;
import buildcraft.core.utils.Utils;

public class TilePump extends TileBuildCraft implements IHasWork, IFluidHandler {

	public static final int REBUID_DELAY = 512;
	public static int MAX_LIQUID = FluidContainerRegistry.BUCKET_VOLUME * 16;
	public SingleUseTank tank = new SingleUseTank("tank", MAX_LIQUID, this);

	private EntityBlock tube;
	private TreeMap<Integer, Deque<BlockPos>> pumpLayerQueues = new TreeMap<Integer, Deque<BlockPos>>();
	private double tubeY = Double.NaN;
	private int aimY = 0;

	private SafeTimeTracker timer = new SafeTimeTracker(REBUID_DELAY);
	private int tick = Utils.RANDOM.nextInt();
	private int numFluidBlocksFound = 0;
	private boolean powered = false;

	public TilePump() {
		super();
		this.setBattery(new RFBattery(1000, 150, 0));
	}
	
	@Override
	public void update() {
		super.update();

		if (powered) {
			pumpLayerQueues.clear();
			destroyTube();
		} else {
			createTube();
		}

		if (worldObj.isRemote) {
			return;
		}

		pushToConsumers();

		if (powered) {
			return;
		}

		if (tube == null) {
			return;
		}

		if (tube.posY - aimY > 0.01) {
			tubeY = tube.posY - 0.01;
			setTubePosition();
			sendNetworkUpdate();
			return;
		}

		tick++;

		if (tick % 16 != 0) {
			return;
		}

		BlockPos index = getNextIndexToPump(false);

		FluidStack fluidToPump = index != null ? BlockUtils.drainBlock(worldObj, index, false) : null;
		if (fluidToPump != null) {
			if (isFluidAllowed(fluidToPump.getFluid()) && tank.fill(fluidToPump, false) == fluidToPump.amount) {
				if (getBattery().useEnergy(100, 100, false) > 0) {
					if (fluidToPump.getFluid() != FluidRegistry.WATER || BuildCraftCore.consumeWaterSources || numFluidBlocksFound < 9) {
						index = getNextIndexToPump(true);
						BlockUtils.drainBlock(worldObj, index, true);
					}

					tank.fill(fluidToPump, true);
				}
			}
		} else {
			if (tick % 128 == 0) {
				// TODO: improve that decision
				rebuildQueue();

				if (getNextIndexToPump(false) == null) {
					for (int y = pos.getY() - 1; y > 0; --y) {
						if (isPumpableFluid(new BlockPos(pos.getX(), y, pos.getZ()))) {
							aimY = y;
							return;
						} else if (isBlocked(new BlockPos(pos.getX(), y, pos.getZ()))) {
							return;
						}
					}
				}
			}
		}
	}

	public void onNeighborBlockChange(Block block) {
		boolean p = worldObj.isBlockPowered(pos);

		if (powered != p) {
			powered = p;

			if (!worldObj.isRemote) {
				sendNetworkUpdate();
			}
		}
	}

	private boolean isBlocked(BlockPos pos) {
		Material mat = worldObj.getBlockState(pos).getBlock().getMaterial();

		return mat.blocksMovement();
	}

	private void pushToConsumers() {
		if (cache == null) {
			cache = TileBuffer.makeBuffer(worldObj, pos, false);
		}

		TankUtils.pushFluidToConsumers(tank, 400, cache);
	}

	private void createTube() {
		if (tube == null) {
			tube = FactoryProxy.proxy.newPumpTube(worldObj);

			if (!Double.isNaN(tubeY)) {
				tube.posY = tubeY;
			} else {
				tube.posY = pos.getY();
			}

			tubeY = tube.posY;

			if (aimY == 0) {
				aimY = pos.getY();
			}

			setTubePosition();

			worldObj.spawnEntityInWorld(tube);

			if (!worldObj.isRemote) {
				sendNetworkUpdate();
			}
		}
	}

	private void destroyTube() {
		if (tube != null) {
			CoreProxy.proxy.removeEntity(tube);
			tube = null;
			tubeY = Double.NaN;
			aimY = 0;
		}
	}

	private BlockPos getNextIndexToPump(boolean remove) {
		if (pumpLayerQueues.isEmpty()) {
			if (timer.markTimeIfDelay(worldObj)) {
				rebuildQueue();
			}

			return null;
		}

		Deque<BlockPos> topLayer = pumpLayerQueues.lastEntry().getValue();

		if (topLayer != null) {
			if (topLayer.isEmpty()) {
				pumpLayerQueues.pollLastEntry();
			}

			if (remove) {
				BlockPos index = topLayer.pollLast();
				return index;
			} else {
				return topLayer.peekLast();
			}
		} else {
			return null;
		}
	}

	private Deque<BlockPos> getLayerQueue(int layer) {
		Deque<BlockPos> pumpQueue = pumpLayerQueues.get(layer);

		if (pumpQueue == null) {
			pumpQueue = new LinkedList<BlockPos>();
			pumpLayerQueues.put(layer, pumpQueue);
		}

		return pumpQueue;
	}

	public void rebuildQueue() {
		numFluidBlocksFound = 0;
		pumpLayerQueues.clear();
		BlockPos p = new BlockPos(pos.getX(), aimY, pos.getZ());
		Fluid pumpingFluid = BlockUtils.getFluid(worldObj.getBlockState(p).getBlock());

		if (pumpingFluid == null) {
			return;
		}

		if (pumpingFluid != tank.getAcceptedFluid() && tank.getAcceptedFluid() != null) {
			return;
		}

		Set<BlockPos> visitedBlocks = new HashSet<BlockPos>();
		Deque<BlockPos> fluidsFound = new LinkedList<BlockPos>();

		queueForPumping(p, visitedBlocks, fluidsFound, pumpingFluid);

//		long timeoutTime = System.nanoTime() + 10000;

		while (!fluidsFound.isEmpty()) {
			Deque<BlockPos> fluidsToExpand = fluidsFound;
			fluidsFound = new LinkedList<BlockPos>();

			for (BlockPos index : fluidsToExpand) {
				queueForPumping(index.up(), visitedBlocks, fluidsFound, pumpingFluid);
				queueForPumping(index.east(), visitedBlocks, fluidsFound, pumpingFluid);
				queueForPumping(index.west(), visitedBlocks, fluidsFound, pumpingFluid);
				queueForPumping(index.north(), visitedBlocks, fluidsFound, pumpingFluid);
				queueForPumping(index.south(), visitedBlocks, fluidsFound, pumpingFluid);

				if (pumpingFluid == FluidRegistry.WATER
						&& !BuildCraftCore.consumeWaterSources
						&& numFluidBlocksFound >= 9) {
					return;
				}

//				if (System.nanoTime() > timeoutTime)
//					return;
			}
		}
	}

	public void queueForPumping(BlockPos index, Set<BlockPos> visitedBlocks, Deque<BlockPos> fluidsFound, Fluid pumpingFluid) {
		if (visitedBlocks.add(index)) {
			if ((index.getX() - pos.getX()) * (index.getX() - pos.getY()) + index.getZ() - pos.getZ() * (index.getZ() - pos.getZ()) > 64 * 64) {
				return;
			}

			Block block = worldObj.getBlockState(index).getBlock();

			if (BlockUtils.getFluid(block) == pumpingFluid) {
				fluidsFound.add(index);
			}

			if (canDrainBlock(block, index, pumpingFluid)) {
				getLayerQueue(index.getY()).add(index);
				numFluidBlocksFound++;
			}
		}
	}

	private boolean isPumpableFluid(BlockPos pos) {
		Fluid fluid = BlockUtils.getFluid(worldObj.getBlockState(pos).getBlock());

		if (fluid == null) {
			return false;
		} else if (!isFluidAllowed(fluid)) {
			return false;
		} else {
			return !(tank.getAcceptedFluid() != null && tank.getAcceptedFluid() != fluid);
		}
	}

	private boolean canDrainBlock(Block block, BlockPos pos, Fluid fluid) {
		if (!isFluidAllowed(fluid)) {
			return false;
		}

		FluidStack fluidStack = BlockUtils.drainBlock(block, worldObj, pos, false);

		if (fluidStack == null || fluidStack.amount <= 0) {
			return false;
		} else {
			return fluidStack.getFluid() == fluid;
		}
	}

	private boolean isFluidAllowed(Fluid fluid) {
		return BuildCraftFactory.pumpDimensionList.isFluidAllowed(fluid, worldObj.provider.getDimensionId());
	}

	@Override
	public void readFromNBT(NBTTagCompound data) {
		super.readFromNBT(data);

		tank.readFromNBT(data);

		powered = data.getBoolean("powered");

		aimY = data.getInteger("aimY");
		tubeY = data.getFloat("tubeY");
	}

	@Override
	public void writeToNBT(NBTTagCompound data) {
		super.writeToNBT(data);

		tank.writeToNBT(data);

		data.setBoolean("powered", powered);

		data.setInteger("aimY", aimY);

		if (tube != null) {
			data.setFloat("tubeY", (float) tube.posY);
		} else {
			data.setFloat("tubeY", pos.getY());
		}
	}

	@Override
	public boolean hasWork() {
		BlockPos next = getNextIndexToPump(false);

		if (next != null) {
			return isPumpableFluid(next);
		} else {
			return false;
		}
	}

	@Override
	public void writeData(ByteBuf buf) {
		buf.writeShort(aimY);
		buf.writeFloat((float) tubeY);
		buf.writeBoolean(powered);
	}

	@Override
	public void readData(ByteBuf data) {
		aimY = data.readShort();
		tubeY = data.readFloat();
		powered = data.readBoolean();

		setTubePosition();
	}

	private void setTubePosition() {
		if (tube != null) {
			tube.iSize = CoreConstants.PIPE_MAX_POS - CoreConstants.PIPE_MIN_POS;
			tube.kSize = CoreConstants.PIPE_MAX_POS - CoreConstants.PIPE_MIN_POS;
			tube.jSize = pos.getY() - tube.posY;

			tube.setPosition(pos.getX() + CoreConstants.PIPE_MIN_POS, tubeY, pos.getZ() + CoreConstants.PIPE_MIN_POS);
		}
	}

	@Override
	public void invalidate() {
		super.invalidate();
		destroy();
	}

	@Override
	public void validate() {
		super.validate();
	}

	@Override
	public void destroy() {
		pumpLayerQueues.clear();
		destroyTube();
	}

	// IFluidHandler implementation.
	@Override
	public int fill(EnumFacing from, FluidStack resource, boolean doFill) {
		// not acceptable
		return 0;
	}

	@Override
	public FluidStack drain(EnumFacing from, int maxDrain, boolean doDrain) {
		return tank.drain(maxDrain, doDrain);
	}

	@Override
	public FluidStack drain(EnumFacing from, FluidStack resource, boolean doDrain) {
		if (resource == null) {
			return null;
		} else if (!resource.isFluidEqual(tank.getFluid())) {
			return null;
		} else {
			return drain(from, resource.amount, doDrain);
		}
	}

	@Override
	public boolean canFill(EnumFacing from, Fluid fluid) {
		return false;
	}

	@Override
	public boolean canDrain(EnumFacing from, Fluid fluid) {
		return true;
	}

	@Override
	public FluidTankInfo[] getTankInfo(EnumFacing from) {
		return new FluidTankInfo[]{tank.getInfo()};
	}
}
