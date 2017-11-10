/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
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
import net.minecraft.nbt.NBTTagCompound;

import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.BlockFluidBase;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

import buildcraft.api.core.BlockIndex;
import buildcraft.api.core.BuildCraftAPI;
import buildcraft.core.lib.block.TileBuildCraft;
import buildcraft.core.lib.fluids.Tank;
import buildcraft.core.lib.fluids.TankUtils;
import buildcraft.core.lib.utils.BlockUtils;
import buildcraft.core.lib.utils.Utils;

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
	private boolean[] blockedSides = new boolean[6];

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

				if (fluid == FluidRegistry.WATER && worldObj.provider.dimensionId == -1) {
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
				BlockIndex index = getNextIndexToFill(true);

				if (index != null && placeFluid(index.x, index.y, index.z, fluid)) {
					tank.drain(FluidContainerRegistry.BUCKET_VOLUME, true);
					rebuildDelay = 0;
				}
			}
		}
	}

	private boolean placeFluid(int x, int y, int z, Fluid fluid) {
		Block block = BlockUtils.getBlock(worldObj, x, y, z);

		if (canPlaceFluidAt(block, x, y, z)) {
			boolean placed;
			Block b = TankUtils.getFluidBlock(fluid, true);

			if (b instanceof BlockFluidBase) {
				BlockFluidBase blockFluid = (BlockFluidBase) b;
				placed = worldObj.setBlock(x, y, z, b, blockFluid.getMaxRenderHeightMeta(), 3);
			} else {
				placed = worldObj.setBlock(x, y, z, b);
			}

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
			if (bottomLayer.isEmpty()) {
				pumpLayerQueues.pollFirstEntry();
			}
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
		if (tank.getFluidType() == null) {
			return;
		}
		while (!fluidsFound.isEmpty()) {
			Deque<BlockIndex> fluidsToExpand = fluidsFound;
			fluidsFound = new LinkedList<BlockIndex>();

			for (BlockIndex index : fluidsToExpand) {
				queueAdjacent(index.x, index.y, index.z);
			}
		}
	}

	public void queueAdjacent(int x, int y, int z) {
		if (tank.getFluidType() == null) {
			return;
		}
		for (int i = 0; i < 6; i++) {
			if (i != 1 && !blockedSides[i]) {
				ForgeDirection dir = ForgeDirection.getOrientation(i);
				queueForFilling(x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ);
			}
		}
	}

	public void queueForFilling(int x, int y, int z) {
		if (y < 0 || y > 255) {
			return;
		}
		BlockIndex index = new BlockIndex(x, y, z);
		if (visitedBlocks.add(index)) {
			if ((x - xCoord) * (x - xCoord) + (z - zCoord) * (z - zCoord) > 64 * 64) {
				return;
			}

			Block block = BlockUtils.getBlock(worldObj, x, y, z);
			if (BlockUtils.getFluid(block) == tank.getFluidType()) {
				fluidsFound.add(index);
			}
			if (canPlaceFluidAt(block, x, y, z)) {
				getLayerQueue(y).addLast(index);
			}
		}
	}

	private boolean canPlaceFluidAt(Block block, int x, int y, int z) {
		return BuildCraftAPI.isSoftBlock(worldObj, x, y, z) && !BlockUtils.isFullFluidBlock(block, worldObj, x, y, z);
	}

	public void onNeighborBlockChange(Block block) {
		boolean p = worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord);
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
		for (int i = 0; i < 6; i++) {
			blockedSides[i] = data.getBoolean("blocked[" + i + "]");
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound data) {
		super.writeToNBT(data);
		tank.writeToNBT(data);
		data.setByte("rebuildDelay", (byte) rebuildDelay);
		data.setBoolean("powered", powered);
		for (int i = 0; i < 6; i++) {
			if (blockedSides[i]) {
				data.setBoolean("blocked[" + i + "]", true);
			}
		}
	}

	@Override
	public void readData(ByteBuf stream) {
		byte flags = stream.readByte();
		for (int i = 0; i < 6; i++) {
			blockedSides[i] = (flags & (1 << i)) != 0;
		}
	}

	@Override
	public void writeData(ByteBuf stream) {
		byte flags = 0;
		for (int i = 0; i < 6; i++) {
			if (blockedSides[i]) {
				flags |= 1 << i;
			}
		}
		stream.writeByte(flags);
	}

	public void switchSide(ForgeDirection side) {
		if (side.ordinal() != 1) {
			blockedSides[side.ordinal()] = !blockedSides[side.ordinal()];

			rebuildQueue();
			sendNetworkUpdate();
			worldObj.markBlockRangeForRenderUpdate(xCoord, yCoord, zCoord, xCoord, yCoord, zCoord);
		}
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

	public boolean isSideBlocked(int side) {
		return blockedSides[side];
	}
}
