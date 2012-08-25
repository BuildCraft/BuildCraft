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

import buildcraft.BuildCraftCore;
import buildcraft.api.core.BuildCraftAPI;
import buildcraft.api.core.Orientations;
import buildcraft.api.core.Position;
import buildcraft.api.liquids.ITankContainer;
import buildcraft.api.liquids.LiquidStack;
import buildcraft.api.power.IPowerProvider;
import buildcraft.api.power.IPowerReceptor;
import buildcraft.api.power.PowerFramework;
import buildcraft.core.BlockIndex;
import buildcraft.core.EntityBlock;
import buildcraft.core.IMachine;
import buildcraft.core.ProxyCore;
import buildcraft.core.Utils;
import buildcraft.core.network.PacketUpdate;
import buildcraft.core.network.TileNetworkData;

import net.minecraft.src.Block;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.TileEntity;

public class TilePump extends TileMachine implements IMachine, IPowerReceptor {

	EntityBlock tube;

	private TreeMap<Integer, LinkedList<BlockIndex>> blocksToPump = new TreeMap<Integer, LinkedList<BlockIndex>>();

	public @TileNetworkData
	int internalLiquid;
	public @TileNetworkData
	double tubeY = Double.NaN;
	public @TileNetworkData
	int aimY = 0;
	public @TileNetworkData
	int liquidId = 0;

	private IPowerProvider powerProvider;

	public TilePump() {
		powerProvider = PowerFramework.currentFramework.createPowerProvider();
		powerProvider.configure(20, 10, 10, 10, 100);
	}

	// TODO, manage this by different levels (pump what's above first...)

	@Override
	public void updateEntity() {
		super.updateEntity();

		if (tube == null) {
			return;
		}

		if (!ProxyCore.proxy.isRemote(worldObj)) {
			if (tube.posY - aimY > 0.01) {
				tubeY = tube.posY - 0.01;

				setTubePosition();

				if (ProxyCore.proxy.isSimulating(worldObj)) {
					sendNetworkUpdate();
				}

				return;
			}

			if (internalLiquid <= 0) {
				BlockIndex index = getNextIndexToPump(false);

				if (isPumpableLiquid(index)) {
					int liquidToPump = Utils.liquidId(worldObj.getBlockId(index.i, index.j, index.k));

					if (internalLiquid == 0 || liquidId == liquidToPump) {
						liquidId = liquidToPump;

						if (powerProvider.useEnergy(10, 10, true) == 10) {
							index = getNextIndexToPump(true);

							if (liquidId != Block.waterStill.blockID || BuildCraftCore.consumeWaterSources) {
								worldObj.setBlockWithNotify(index.i, index.j, index.k, 0);
							}

							internalLiquid = internalLiquid += BuildCraftAPI.BUCKET_VOLUME;

							if (ProxyCore.proxy.isSimulating(worldObj)) {
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
								} else if (worldObj.getBlockId(xCoord, y, zCoord) != 0) {
									return;
								}
							}
						}
					}
				}
			}
		}

		if (internalLiquid >= 0) {
			for (int i = 0; i < 6; ++i) {
				Position p = new Position(xCoord, yCoord, zCoord, Orientations.values()[i]);
				p.moveForwards(1);

				TileEntity tile = worldObj.getBlockTileEntity((int) p.x, (int) p.y, (int) p.z);
				
				if(tile instanceof ITankContainer) {
					internalLiquid -= ((ITankContainer)tile).fill(p.orientation.reverse(), new LiquidStack(liquidId, internalLiquid), true);
					if(internalLiquid <= 0)
						break;
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

		if (ProxyCore.proxy.isSimulating(worldObj)) {
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
			} else {
				return topLayer.getLast();
			}
		} else {
			return null;
		}
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

		if (!isLiquid(new BlockIndex(x, y, z))) {
			return;
		}

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

	public void addToPumpIfLiquid(BlockIndex index, TreeSet<BlockIndex> markedBlocks, TreeSet<BlockIndex> lastFound,
			LinkedList<BlockIndex> pumpList, int liquidId) {

		if (liquidId != worldObj.getBlockId(index.i, index.j, index.k)) {
			return;
		}

		if (!markedBlocks.contains(index)) {
			markedBlocks.add(index);

			if ((index.i - xCoord) * (index.i - xCoord) + (index.k - zCoord) * (index.k - zCoord) > 64 * 64) {
				return;
			}

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
		return index != null && (Utils.liquidId(worldObj.getBlockId(index.i, index.j, index.k)) != 0);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		super.readFromNBT(nbttagcompound);

		internalLiquid = nbttagcompound.getInteger("internalLiquid");
		aimY = nbttagcompound.getInteger("aimY");

		tubeY = nbttagcompound.getFloat("tubeY");
		liquidId = nbttagcompound.getInteger("liquidId");

		PowerFramework.currentFramework.loadPowerProvider(this, nbttagcompound);
		powerProvider.configure(20, 10, 10, 10, 100);

	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		super.writeToNBT(nbttagcompound);

		PowerFramework.currentFramework.savePowerProvider(this, nbttagcompound);

		nbttagcompound.setInteger("internalLiquid", internalLiquid);
		nbttagcompound.setInteger("aimY", aimY);

		if (tube != null) {
			nbttagcompound.setFloat("tubeY", (float) tube.posY);
		} else {
			nbttagcompound.setFloat("tubeY", yCoord);
		}

		nbttagcompound.setInteger("liquidId", liquidId);
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
	public void doWork() {}

	@Override
	public void handleDescriptionPacket(PacketUpdate packet) {
		super.handleDescriptionPacket(packet);

		setTubePosition();
	}

	@Override
	public void handleUpdatePacket(PacketUpdate packet) {
		super.handleDescriptionPacket(packet);

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
			ProxyCore.proxy.removeEntity(tube);
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
}
