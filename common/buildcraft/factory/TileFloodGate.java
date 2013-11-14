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
import buildcraft.core.fluids.FluidUtils;
import buildcraft.core.fluids.Tank;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.core.utils.BlockUtil;
import buildcraft.core.utils.Utils;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeMap;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

public class TileFloodGate extends TileBuildCraft implements IFluidHandler {

	public static final int[] REBUILD_DELAY = new int[8];
	public static final int MAX_LIQUID = FluidContainerRegistry.BUCKET_VOLUME * 2;
	private final TreeMap<Integer, Deque<BlockIndex>> pumpLayerQueues = new TreeMap<Integer, Deque<BlockIndex>>();
	private final Set<BlockIndex> visitedBlocks = new HashSet<BlockIndex>();
	private Deque<BlockIndex> fluidsFound = new LinkedList<BlockIndex>();
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
	public void updateEntity() {
		super.updateEntity();

		if (CoreProxy.proxy.isRenderWorld(worldObj))
			return;

		if (powered)
			return;

		tick++;
		if (tick % 16 == 0) {
			FluidStack fluidtoFill = tank.drain(FluidContainerRegistry.BUCKET_VOLUME, false);
			if (fluidtoFill != null && fluidtoFill.amount == FluidContainerRegistry.BUCKET_VOLUME) {
				Fluid fluid = fluidtoFill.getFluid();
				if (fluid == null || !fluid.canBePlacedInWorld())
					return;

				if (fluid == FluidRegistry.WATER && worldObj.provider.dimensionId == -1) {
					tank.drain(FluidContainerRegistry.BUCKET_VOLUME, true);
					return;
				}

				if (tick % REBUILD_DELAY[rebuildDelay] == 0) {
					rebuildDelay++;
					if (rebuildDelay >= REBUILD_DELAY.length)
						rebuildDelay = REBUILD_DELAY.length - 1;
					rebuildQueue();
				}
				BlockIndex index = getNextIndexToFill(true);

				if (index != null && placeFluid(index.x, index.y, index.z, fluid)) {
					tank.drain(FluidContainerRegistry.BUCKET_VOLUME, true);
					rebuildDelay = 0;
				}
			}
		}
	}

	private boolean placeFluid(int x, int y, int z, Fluid fluid) {
		int blockId = worldObj.getBlockId(x, y, z);
		if (canPlaceFluidAt(blockId, x, y, z)) {
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
			if (canPlaceFluidAt(blockId, x, y, z)) {
				getLayerQueue(y).addLast(index);
			}
		}
	}

	private boolean canPlaceFluidAt(int blockId, int x, int y, int z) {
		return BlockUtil.isSoftBlock(blockId, worldObj, x, y, z) && !BlockUtil.isFullFluidBlock(blockId, worldObj, x, y, z);
	}

	public void onNeighborBlockChange(int id) {
		boolean p = worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord);
		if (powered != p) {
			powered = p;
			if (!p)
				rebuildQueue();
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
