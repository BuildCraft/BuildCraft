/**
 * Copyright (c) SpaceToad, 2011 http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License
 * 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.factory;

import buildcraft.api.core.SafeTimeTracker;
import buildcraft.core.BlockIndex;
import buildcraft.core.TileBuildCraft;
import buildcraft.core.liquids.FluidUtils;
import buildcraft.core.liquids.Tank;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.core.utils.BlockUtil;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeMap;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

public class TileFloodGate extends TileBuildCraft implements IFluidHandler {

	public static final int[] REBUID_DELAY = new int[7];
	public static final int MAX_LIQUID = FluidContainerRegistry.BUCKET_VOLUME * 2;
	private final TreeMap<Integer, Deque<BlockIndex>> pumpLayerQueues = new TreeMap<Integer, Deque<BlockIndex>>();
	private final Set<BlockIndex> visitedBlocks = new HashSet<BlockIndex>();
	private Deque<BlockIndex> fluidsFound = new LinkedList<BlockIndex>();
	private final Tank tank;
	private int rebuildDelay;

	static {
		REBUID_DELAY[0] = 128;
		REBUID_DELAY[1] = 256;
		REBUID_DELAY[2] = 512;
		REBUID_DELAY[3] = 1024;
		REBUID_DELAY[4] = 2048;
		REBUID_DELAY[5] = 4096;
		REBUID_DELAY[6] = 8192;
	}

	public TileFloodGate() {
		tank = new Tank("tank", MAX_LIQUID);
	}

	@Override
	public void updateEntity() {
		super.updateEntity();

		if (CoreProxy.proxy.isRenderWorld(worldObj))
			return;

		if (worldObj.getWorldTime() % 16 == 0) {
			FluidStack fluidtoFill = tank.drain(FluidContainerRegistry.BUCKET_VOLUME, false);
			if (fluidtoFill != null && fluidtoFill.amount == FluidContainerRegistry.BUCKET_VOLUME && fluidtoFill.getFluid() != null) {
				if (worldObj.getWorldTime() % REBUID_DELAY[rebuildDelay] == 0) {
					rebuildDelay++;
					if (rebuildDelay >= REBUID_DELAY.length)
						rebuildDelay = REBUID_DELAY.length - 1;
					rebuildQueue();
				}
				BlockIndex index = getNextIndexToFill(true);

				if (index != null && placeFluid(index.x, index.y, index.z, fluidtoFill.getFluid())) {
					tank.drain(FluidContainerRegistry.BUCKET_VOLUME, true);
					rebuildDelay = 0;
				}
			}
		}
	}

	private boolean placeFluid(int x, int y, int z, Fluid fluid) {
		int blockId = worldObj.getBlockId(x, y, z);
		if (canPlaceFluid(blockId, x, y, z)) {
			boolean placed = worldObj.setBlock(x, y, z, FluidUtils.getFluidBlockId(fluid, true));
			if (placed) {
				queueAdjacent(x, y, z);
				expandQueue();
			}
			return placed;
		}
		return false;
	}

	private BlockIndex getNextIndexToFill(boolean remove) {
		if (pumpLayerQueues.isEmpty()) {
			return null;
		}

		Deque<BlockIndex> bottomLayer = pumpLayerQueues.firstEntry().getValue();

		if (bottomLayer != null) {
			if (bottomLayer.isEmpty())
				pumpLayerQueues.pollFirstEntry();
			if (remove) {
				BlockIndex index = bottomLayer.pollFirst();
				return index;
			}
			return bottomLayer.peekFirst();
		}

		return null;
	}

	private Deque<BlockIndex> getLayerQueue(int layer) {
		Deque<BlockIndex> pumpQueue = pumpLayerQueues.get(layer);
		if (pumpQueue == null) {
			pumpQueue = new LinkedList<BlockIndex>();
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

		queueAdjacent(xCoord, yCoord, zCoord);

		expandQueue();
	}

	private void expandQueue() {
		if (tank.getFluidType() == null)
			return;
		while (!fluidsFound.isEmpty()) {
			Deque<BlockIndex> fluidsToExpand = fluidsFound;
			fluidsFound = new LinkedList<BlockIndex>();

			for (BlockIndex index : fluidsToExpand) {
				queueAdjacent(index.x, index.y, index.z);
			}
		}
	}

	public void queueAdjacent(int x, int y, int z) {
		if (tank.getFluidType() == null)
			return;
		queueForFilling(x, y - 1, z);
		queueForFilling(x + 1, y, z);
		queueForFilling(x - 1, y, z);
		queueForFilling(x, y, z + 1);
		queueForFilling(x, y, z - 1);
	}

	public void queueForFilling(int x, int y, int z) {
		BlockIndex index = new BlockIndex(x, y, z);
		if (visitedBlocks.add(index)) {
			if ((x - xCoord) * (x - xCoord) + (z - zCoord) * (z - zCoord) > 64 * 64)
				return;

			int blockId = worldObj.getBlockId(x, y, z);
			if (BlockUtil.getFluid(blockId) == tank.getFluidType()) {
				fluidsFound.add(index);
			}
			if (canPlaceFluid(blockId, x, y, z)) {
				getLayerQueue(y).addLast(index);
			}
		}
	}

	private boolean canPlaceFluid(int blockId, int x, int y, int z) {
		return BlockUtil.isSoftBlock(blockId, worldObj, x, y, z) && !BlockUtil.isFullFluidBlock(blockId, worldObj, x, y, z);
	}

	@Override
	public void readFromNBT(NBTTagCompound data) {
		super.readFromNBT(data);
		tank.readFromNBT(data);
	}

	@Override
	public void writeToNBT(NBTTagCompound data) {
		super.writeToNBT(data);
		tank.writeToNBT(data);
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
	public int fill(ForgeDirection from, FluidStack resource, boolean doFill) {
		return tank.fill(resource, doFill);
	}

	@Override
	public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) {
		return null;
	}

	@Override
	public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain) {
		return null;
	}

	@Override
	public boolean canFill(ForgeDirection from, Fluid fluid) {
		return true;
	}

	@Override
	public boolean canDrain(ForgeDirection from, Fluid fluid) {
		return false;
	}

	@Override
	public FluidTankInfo[] getTankInfo(ForgeDirection from) {
		return new FluidTankInfo[]{tank.getInfo()};
	}
}
