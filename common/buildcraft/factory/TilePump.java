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
import java.util.TreeMap;
import java.util.TreeSet;

import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.liquids.ILiquidTank;
import net.minecraftforge.liquids.ITankContainer;
import net.minecraftforge.liquids.LiquidContainerRegistry;
import net.minecraftforge.liquids.LiquidStack;
import net.minecraftforge.liquids.LiquidTank;
import buildcraft.BuildCraftCore;
import buildcraft.api.core.Position;
import buildcraft.api.power.IPowerProvider;
import buildcraft.api.power.IPowerReceptor;
import buildcraft.api.power.PowerFramework;
import buildcraft.core.BlockIndex;
import buildcraft.core.EntityBlock;
import buildcraft.core.IMachine;
import buildcraft.core.network.PacketPayload;
import buildcraft.core.network.PacketUpdate;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.core.utils.Utils;

public class TilePump extends TileMachine implements IMachine, IPowerReceptor, ITankContainer {

	public static int MAX_LIQUID = LiquidContainerRegistry.BUCKET_VOLUME;

	EntityBlock tube;

	private TreeMap<Integer, LinkedList<BlockIndex>> blocksToPump = new TreeMap<Integer, LinkedList<BlockIndex>>();

	LiquidTank tank;
	double tubeY = Double.NaN;
	int aimY = 0;

	private IPowerProvider powerProvider;

	public TilePump() {
		powerProvider = PowerFramework.currentFramework.createPowerProvider();
		powerProvider.configure(20, 1, 10, 10, 100);
		tank = new LiquidTank(MAX_LIQUID);
	}

	// TODO, manage this by different levels (pump what's above first...)

	@Override
	public void updateEntity() {
		super.updateEntity();

		if (tube == null)
			return;

		if (!CoreProxy.proxy.isRenderWorld(worldObj)) {
			if (tube.posY - aimY > 0.01) {
				tubeY = tube.posY - 0.01;

				setTubePosition();

				if (CoreProxy.proxy.isSimulating(worldObj)) {
					sendNetworkUpdate();
				}

				return;
			}

			if (tank.getLiquid() == null || tank.getLiquid().amount <= 0) {
				BlockIndex index = getNextIndexToPump(false);

				if (isPumpableLiquid(index)) {
					LiquidStack liquidToPump = Utils.liquidFromBlockId(worldObj.getBlockId(index.i, index.j, index.k));

					if (tank.fill(liquidToPump, false) == liquidToPump.amount) {

						if (powerProvider.useEnergy(10, 10, true) == 10) {
							index = getNextIndexToPump(true);

							if (liquidToPump.itemID != Block.waterStill.blockID || BuildCraftCore.consumeWaterSources) {
								worldObj.setBlockWithNotify(index.i, index.j, index.k, 0);
							}

							tank.fill(liquidToPump, true);

							if (CoreProxy.proxy.isSimulating(worldObj)) {
								sendNetworkUpdate();
							}
						}
					}
				} else {
					if (worldObj.getWorldTime() % 100 == 0) {
						// TODO: improve that decision

						initializePumpFromPosition(xCoord, aimY, zCoord);

						if (getNextIndexToPump(false) == null) {
							for (int y = yCoord - 1; y > 0; --y) {
								if (isLiquid(new BlockIndex(xCoord, y, zCoord))) {
									aimY = y;
									return;
								} else if (worldObj.getBlockId(xCoord, y, zCoord) != 0)
									return;
							}
						}
					}
				}
			}
		}

		LiquidStack liquid = tank.getLiquid();
		if (liquid != null && liquid.amount >= 0) {
			for (int i = 0; i < 6; ++i) {
				Position p = new Position(xCoord, yCoord, zCoord, ForgeDirection.values()[i]);
				p.moveForwards(1);

				TileEntity tile = worldObj.getBlockTileEntity((int) p.x, (int) p.y, (int) p.z);

				if (tile instanceof ITankContainer) {
					int moved = ((ITankContainer) tile).fill(p.orientation.getOpposite(), liquid, true);
					tank.drain(moved, true);
					if (liquid.amount <= 0) {
						break;
					}
				}
			}
		}
	}

	@Override
	public void initialize() {
		tube = new EntityBlock(worldObj);
		tube.texture = 6 * 16 + 6;

		if (!Double.isNaN(tubeY)) {
			tube.posY = tubeY;
		} else {
			tube.posY = yCoord;
		}

		tubeY = tube.posY;

		if (aimY == 0) {
			aimY = yCoord;
		}

		setTubePosition();

		worldObj.spawnEntityInWorld(tube);

		if (CoreProxy.proxy.isSimulating(worldObj)) {
			sendNetworkUpdate();
		}
	}

	private BlockIndex getNextIndexToPump(boolean remove) {
		LinkedList<BlockIndex> topLayer = null;

		int topLayerHeight = 0;

		for (Integer layer : blocksToPump.keySet()) {
			if (layer > topLayerHeight && blocksToPump.get(layer).size() != 0) {
				topLayerHeight = layer;
				topLayer = blocksToPump.get(layer);
			}
		}

		if (topLayer != null) {
			if (remove) {
				BlockIndex index = topLayer.pop();

				if (topLayer.size() == 0) {
					blocksToPump.remove(topLayerHeight);
				}

				return index;
			} else
				return topLayer.getLast();
		} else
			return null;
	}

	private void initializePumpFromPosition(int x, int y, int z) {
		int liquidId = 0;

		TreeSet<BlockIndex> markedBlocks = new TreeSet<BlockIndex>();
		TreeSet<BlockIndex> lastFound = new TreeSet<BlockIndex>();

		if (!blocksToPump.containsKey(y)) {
			blocksToPump.put(y, new LinkedList<BlockIndex>());
		}

		LinkedList<BlockIndex> pumpList = blocksToPump.get(y);

		liquidId = worldObj.getBlockId(x, y, z);

		if (!isLiquid(new BlockIndex(x, y, z)))
			return;

		addToPumpIfLiquid(new BlockIndex(x, y, z), markedBlocks, lastFound, pumpList, liquidId);

		while (lastFound.size() > 0) {
			TreeSet<BlockIndex> visitIteration = new TreeSet<BlockIndex>(lastFound);
			lastFound.clear();

			for (BlockIndex index : visitIteration) {
				addToPumpIfLiquid(new BlockIndex(index.i + 1, index.j, index.k), markedBlocks, lastFound, pumpList, liquidId);
				addToPumpIfLiquid(new BlockIndex(index.i - 1, index.j, index.k), markedBlocks, lastFound, pumpList, liquidId);
				addToPumpIfLiquid(new BlockIndex(index.i, index.j, index.k + 1), markedBlocks, lastFound, pumpList, liquidId);
				addToPumpIfLiquid(new BlockIndex(index.i, index.j, index.k - 1), markedBlocks, lastFound, pumpList, liquidId);

				if (!blocksToPump.containsKey(index.j + 1)) {
					blocksToPump.put(index.j + 1, new LinkedList<BlockIndex>());
				}

				pumpList = blocksToPump.get(index.j + 1);

				addToPumpIfLiquid(new BlockIndex(index.i, index.j + 1, index.k), markedBlocks, lastFound, pumpList, liquidId);
			}
		}
	}

	public void addToPumpIfLiquid(BlockIndex index, TreeSet<BlockIndex> markedBlocks, TreeSet<BlockIndex> lastFound, LinkedList<BlockIndex> pumpList,
			int liquidId) {

		if (liquidId != worldObj.getBlockId(index.i, index.j, index.k))
			return;

		if (!markedBlocks.contains(index)) {
			markedBlocks.add(index);

			if ((index.i - xCoord) * (index.i - xCoord) + (index.k - zCoord) * (index.k - zCoord) > 64 * 64)
				return;

			if (isPumpableLiquid(index)) {
				pumpList.push(index);
			}

			if (isLiquid(index)) {
				lastFound.add(index);
			}
		}
	}

	private boolean isPumpableLiquid(BlockIndex index) {
		return isLiquid(index) && worldObj.getBlockMetadata(index.i, index.j, index.k) == 0;
	}

	private boolean isLiquid(BlockIndex index) {
		return index != null && (Utils.liquidFromBlockId(worldObj.getBlockId(index.i, index.j, index.k)) != null);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		super.readFromNBT(nbttagcompound);

		if (nbttagcompound.hasKey("internalLiquid")) {
			tank.setLiquid(new LiquidStack(nbttagcompound.getInteger("liquidId"), nbttagcompound.getInteger("internalLiquid")));
		} else if (nbttagcompound.hasKey("tank")) {
			tank.setLiquid(LiquidStack.loadLiquidStackFromNBT(nbttagcompound.getCompoundTag("tank")));
		}
		aimY = nbttagcompound.getInteger("aimY");

		tubeY = nbttagcompound.getFloat("tubeY");

		PowerFramework.currentFramework.loadPowerProvider(this, nbttagcompound);
		powerProvider.configure(20, 1, 10, 10, 100);

	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		super.writeToNBT(nbttagcompound);

		PowerFramework.currentFramework.savePowerProvider(this, nbttagcompound);

		if (tank.getLiquid() != null) {
			nbttagcompound.setTag("tank", tank.getLiquid().writeToNBT(new NBTTagCompound()));
		}

		nbttagcompound.setInteger("aimY", aimY);

		if (tube != null) {
			nbttagcompound.setFloat("tubeY", (float) tube.posY);
		} else {
			nbttagcompound.setFloat("tubeY", yCoord);
		}
	}

	@Override
	public boolean isActive() {
		return isPumpableLiquid(getNextIndexToPump(false));
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
	public void doWork() {
	}

	@Override
	public PacketPayload getPacketPayload() {
		PacketPayload payload = new PacketPayload(4, 1, 0);
		if (tank.getLiquid() != null) {
			payload.intPayload[0] = tank.getLiquid().itemID;
			payload.intPayload[1] = tank.getLiquid().itemMeta;
			payload.intPayload[2] = tank.getLiquid().amount;
		} else {
			payload.intPayload[0] = 0;
			payload.intPayload[1] = 0;
			payload.intPayload[2] = 0;
		}
		payload.intPayload[3] = aimY;
		payload.floatPayload[0] = (float) tubeY;

		return payload;
	}

	@Override
	public void handleDescriptionPacket(PacketUpdate packet) {
		handleUpdatePacket(packet);
	}

	@Override
	public void handleUpdatePacket(PacketUpdate packet) {
		if (packet.payload.intPayload[0] > 0) {
			tank.setLiquid(new LiquidStack(packet.payload.intPayload[0], packet.payload.intPayload[2], packet.payload.intPayload[1]));
		} else {
			tank.setLiquid(null);
		}

		aimY = packet.payload.intPayload[3];
		tubeY = packet.payload.floatPayload[0];

		setTubePosition();
	}

	private void setTubePosition() {
		if (tube != null) {
			tube.iSize = Utils.pipeMaxPos - Utils.pipeMinPos;
			tube.kSize = Utils.pipeMaxPos - Utils.pipeMinPos;
			tube.jSize = yCoord - tube.posY;

			tube.setPosition(xCoord + Utils.pipeMinPos, tubeY, zCoord + Utils.pipeMinPos);
		}
	}

	@Override
	public void invalidate() {
		super.invalidate();
		destroy();
	}

	@Override
	public void destroy() {
		if (tube != null) {
			CoreProxy.proxy.removeEntity(tube);
			tube = null;
			tubeY = Double.NaN;
			aimY = 0;
			blocksToPump.clear();
		}
	}

	@Override
	public boolean manageLiquids() {
		return true;
	}

	@Override
	public boolean manageSolids() {
		return false;
	}

	@Override
	public boolean allowActions() {
		return false;
	}

	// ITankContainer implementation.

	@Override
	public int fill(ForgeDirection from, LiquidStack resource, boolean doFill) {
		// not acceptable
		return 0;
	}

	@Override
	public int fill(int tankIndex, LiquidStack resource, boolean doFill) {
		// not acceptable
		return 0;
	}

	@Override
	public LiquidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) {
		return drain(0, maxDrain, doDrain);
	}

	@Override
	public LiquidStack drain(int tankIndex, int maxDrain, boolean doDrain) {
		if (tankIndex == 0)
			return tank.drain(maxDrain, doDrain);

		return null;
	}

	@Override
	public ILiquidTank[] getTanks(ForgeDirection direction) {
		return new ILiquidTank[] { tank };
	}

	@Override
	public ILiquidTank getTank(ForgeDirection direction, LiquidStack type) {
		return tank;
	}
}
