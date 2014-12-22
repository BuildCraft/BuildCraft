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

import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;

import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.BlockFluidBase;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

import buildcraft.api.core.BuildCraftAPI;
import buildcraft.core.TileBuildCraft;
import buildcraft.core.fluids.Tank;
import buildcraft.core.fluids.TankUtils;
import buildcraft.core.utils.BlockUtils;
import buildcraft.core.utils.Utils;

public class TileFloodGate extends TileBuildCraft implements IFluidHandler {

	public static final int[] REBUILD_DELAY = new int[8];
	public static final int MAX_LIQUID = FluidContainerRegistry.BUCKET_VOLUME * 2;
	private final TreeMap<Integer, Deque<BlockPos>> pumpLayerQueues = new TreeMap<Integer, Deque<BlockPos>>();
	private final Set<BlockPos> visitedBlocks = new HashSet<BlockPos>();
	private Deque<BlockPos> fluidsFound = new LinkedList<BlockPos>();
	private final Tank tank = new Tank("tank", MAX_LIQUID, this);
	private int rebuildDelay;
	private int tick = Utils.RANDOM.nextInt();
	private boolean powered = false;

	static {
		REBUILD_DELAY[0] = 128;
		REBUILD_DELAY[1] = 256;
		REBUILD_DELAY[2] = 512;
		REBUILD_DELAY[3] = 1024;
		REBUILD_DELAY[4] = 2048;
		REBUILD_DELAY[5] = 4096;
		REBUILD_DELAY[6] = 8192;
		REBUILD_DELAY[7] = 16384;
	}

	public TileFloodGate() {
	}

	@Override
	public void update() {
		super.update();

		if (worldObj.isRemote) {
			return;
		}

		if (powered) {
			return;
		}

		tick++;
		if (tick % 16 == 0) {
			FluidStack fluidtoFill = tank.drain(FluidContainerRegistry.BUCKET_VOLUME, false);
			if (fluidtoFill != null && fluidtoFill.amount == FluidContainerRegistry.BUCKET_VOLUME) {
				Fluid fluid = fluidtoFill.getFluid();
				if (fluid == null || !fluid.canBePlacedInWorld()) {
					return;
				}

				if (fluid == FluidRegistry.WATER && worldObj.provider.getDimensionId() == -1) {
					tank.drain(FluidContainerRegistry.BUCKET_VOLUME, true);
					return;
				}

				if (tick % REBUILD_DELAY[rebuildDelay] == 0) {
					rebuildDelay++;
					if (rebuildDelay >= REBUILD_DELAY.length) {
						rebuildDelay = REBUILD_DELAY.length - 1;
					}
					rebuildQueue();
				}
				BlockPos index = getNextIndexToFill(true);

				if (index != null && placeFluid(index, fluid)) {
					tank.drain(FluidContainerRegistry.BUCKET_VOLUME, true);
					rebuildDelay = 0;
				}
			}
		}
	}

	private boolean placeFluid(BlockPos pos, Fluid fluid) {
		Block block = worldObj.getBlockState(pos).getBlock();

		if (canPlaceFluidAt(block, pos)) {
			boolean placed;
			Block b = TankUtils.getFluidBlock(fluid, true);

			if (b instanceof BlockFluidBase) {
				BlockFluidBase blockFluid = (BlockFluidBase) b;
				placed = worldObj.setBlockState(pos, b.getDefaultState(), 3);
			} else {
				placed = worldObj.setBlockState(pos, b.getDefaultState());
			}

			if (placed) {
				queueAdjacent(pos);
				expandQueue();
			}

			return placed;
		}

		return false;
	}

	private BlockPos getNextIndexToFill(boolean remove) {
		if (pumpLayerQueues.isEmpty()) {
			return null;
		}

		Deque<BlockPos> bottomLayer = pumpLayerQueues.firstEntry().getValue();

		if (bottomLayer != null) {
			if (bottomLayer.isEmpty()) {
				pumpLayerQueues.pollFirstEntry();
			}
			if (remove) {
				BlockPos index = bottomLayer.pollFirst();
				return index;
			}
			return bottomLayer.peekFirst();
		}

		return null;
	}

	private Deque<BlockPos> getLayerQueue(int layer) {
		Deque<BlockPos> pumpQueue = pumpLayerQueues.get(layer);
		if (pumpQueue == null) {
			pumpQueue = new LinkedList<BlockPos>();
			pumpLayerQueues.put(layer, pumpQueue);
		}
		return pumpQueue;
	}

	/**
	 * Nasty expensive function, don't call if you don't have to.
	 */
	void rebuildQueue() {
		pumpLayerQueues.clear();
		visitedBlocks.clear();
		fluidsFound.clear();

		queueAdjacent(pos);

		expandQueue();
	}

	private void expandQueue() {
		if (tank.getFluidType() == null) {
			return;
		}
		while (!fluidsFound.isEmpty()) {
			Deque<BlockPos> fluidsToExpand = fluidsFound;
			fluidsFound = new LinkedList<BlockPos>();

			for (BlockPos index : fluidsToExpand) {
				queueAdjacent(index);
			}
		}
	}

	public void queueAdjacent(BlockPos pos) {
		if (tank.getFluidType() == null) {
			return;
		}
		queueForFilling(pos.down());
		queueForFilling(pos.east());
		queueForFilling(pos.north());
		queueForFilling(pos.south());
		queueForFilling(pos.west());
	}

	public void queueForFilling(BlockPos pos) {
        if (pos.getY() < 0 || pos.getY() > 255) {
            return;
        }

		if (visitedBlocks.add(pos)) {
			if (pos.distanceSq(this.pos) > 64) {
				return;
			}

			Block block = worldObj.getBlockState(pos).getBlock();
			if (BlockUtils.getFluid(block) == tank.getFluidType()) {
				fluidsFound.add(pos);
			}
			if (canPlaceFluidAt(block, pos)) {
				getLayerQueue(pos.getY()).addLast(pos);
			}
		}
	}

	private boolean canPlaceFluidAt(Block block, BlockPos pos) {
		return BuildCraftAPI.isSoftBlock(worldObj, pos) && !BlockUtils.isFullFluidBlock(block, worldObj, pos);
	}

	public void onNeighborBlockChange(Block block) {
		boolean p = worldObj.isBlockPowered(pos);
		if (powered != p) {
			powered = p;
			if (!p) {
				rebuildQueue();
			}
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound data) {
		super.readFromNBT(data);
		tank.readFromNBT(data);
		rebuildDelay = data.getByte("rebuildDelay");
		powered = data.getBoolean("powered");
	}

	@Override
	public void writeToNBT(NBTTagCompound data) {
		super.writeToNBT(data);
		tank.writeToNBT(data);
		data.setByte("rebuildDelay", (byte) rebuildDelay);
		data.setBoolean("powered", powered);
	}

	@Override
	public void invalidate() {
		super.invalidate();
		destroy();
	}

	@Override
	public void destroy() {
		pumpLayerQueues.clear();
	}

	// IFluidHandler implementation.
	@Override
	public int fill(EnumFacing from, FluidStack resource, boolean doFill) {
		return tank.fill(resource, doFill);
	}

	@Override
	public FluidStack drain(EnumFacing from, int maxDrain, boolean doDrain) {
		return null;
	}

	@Override
	public FluidStack drain(EnumFacing from, FluidStack resource, boolean doDrain) {
		return null;
	}

	@Override
	public boolean canFill(EnumFacing from, Fluid fluid) {
		return true;
	}

	@Override
	public boolean canDrain(EnumFacing from, Fluid fluid) {
		return false;
	}

	@Override
	public FluidTankInfo[] getTankInfo(EnumFacing from) {
		return new FluidTankInfo[]{tank.getInfo()};
	}
}
