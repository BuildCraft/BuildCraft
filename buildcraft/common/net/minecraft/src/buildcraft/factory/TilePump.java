package net.minecraft.src.buildcraft.factory;

import java.util.LinkedList;
import java.util.TreeMap;
import java.util.TreeSet;


import net.minecraft.src.BuildCraftCore;
import net.minecraft.src.BuildCraftEnergy;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.Packet;
import net.minecraft.src.Packet230ModLoader;
import net.minecraft.src.TileEntity;
import net.minecraft.src.buildcraft.api.IPowerReceptor;
import net.minecraft.src.buildcraft.api.Orientations;
import net.minecraft.src.buildcraft.api.Position;
import net.minecraft.src.buildcraft.api.PowerProvider;
import net.minecraft.src.buildcraft.core.BlockIndex;
import net.minecraft.src.buildcraft.core.EntityBlock;
import net.minecraft.src.buildcraft.core.IMachine;
import net.minecraft.src.buildcraft.core.ISynchronizedTile;
import net.minecraft.src.buildcraft.core.TileBuildCraft;
import net.minecraft.src.buildcraft.core.Utils;
import net.minecraft.src.buildcraft.transport.TilePipe;

public class TilePump extends TileBuildCraft implements IMachine, IPowerReceptor, ISynchronizedTile {
	
	EntityBlock tube;
	
	int internalLiquid;
	
	int aimY = 0;
	
	private TreeMap<Integer, LinkedList<BlockIndex>> blocksToPump = new TreeMap<Integer, LinkedList<BlockIndex>> ();

	private float tubeY = 0.0F;

	private PowerProvider powerProvider;
	
	public TilePump () {
		powerProvider = BuildCraftCore.powerFramework.createPowerProvider();
		powerProvider.configure(20, 10, 10, 10, 100);
	}
	
	// TODO, manage this by different levels (pump what's above first...)

	@Override
	public void updateEntity () {
		super.updateEntity();
		
		if (tube.posY - aimY > 0.01) {
			tube.iSize = Utils.pipeMaxPos - Utils.pipeMinPos;
			tube.kSize = Utils.pipeMaxPos - Utils.pipeMinPos;
			tube.jSize = yCoord - tube.posY + 0.01;
			tube.setPosition(xCoord + Utils.pipeMinPos, tube.posY - 0.01,
					zCoord + Utils.pipeMinPos);	
			
			return;
		}
		
		if (internalLiquid <= TilePipe.flowRate) {
			BlockIndex index = getNextIndexToPump(false);
			
			if (isPumpableOil(index)) {
				if (powerProvider.useEnergy(10, 10, true) == 10) {
					index = getNextIndexToPump(true);
					worldObj.setBlockWithNotify(index.i, index.j, index.k, 0);
					internalLiquid = internalLiquid += BuildCraftCore.OIL_BUCKET_QUANTITY;
				}
			} else {
				if (worldObj.getWorldTime() % 100 == 0) {
					// TODO: improve that decision
					
					initializePumpFromPosition(xCoord, aimY, zCoord);
					
					if (getNextIndexToPump(false) == null) {
						for (int y = yCoord; y > 0; --y) {
							if (isPumpableOil(new BlockIndex (xCoord, y, zCoord))) {
								aimY = y;
								return;
							}
						}
					}
				}
			}
		} 
		
		if (internalLiquid >= TilePipe.flowRate) {
			for (int i = 0; i < 6; ++i) {
				Position p = new Position(xCoord, yCoord, zCoord,
						Orientations.values()[i]);
				p.moveForwards(1);

				TileEntity tile = worldObj.getBlockTileEntity((int) p.x, (int) p.y,
						(int) p.z);

				if (tile instanceof TilePipe) {
					internalLiquid -= ((TilePipe) tile).fill(
							p.orientation.reverse(), TilePipe.flowRate);
					
					break;
				}
			}
		}
	}
	
	public void initialize () {
		tube = new EntityBlock(worldObj);
		tube.texture = 6 * 16 + 6;
				
		
		if (tubeY != 0.0) {
			tube.posY = tubeY;
		} else {
			tube.posY = yCoord;
		}
		
		if (aimY == 0) {
			aimY = yCoord;
		}
		
		tube.iSize = Utils.pipeMaxPos - Utils.pipeMinPos;
		tube.kSize = Utils.pipeMaxPos - Utils.pipeMinPos;
		tube.jSize = yCoord - tube.posY + 0.01;
		
		tube.setPosition(xCoord + Utils.pipeMinPos, tube.posY - 0.01,
				zCoord + Utils.pipeMinPos);	
		
		worldObj.entityJoinedWorld(tube);
	}
	
	private BlockIndex getNextIndexToPump (boolean remove) {
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
	
	private void initializePumpFromPosition (int x, int y, int z) {
		TreeSet <BlockIndex> markedBlocks = new TreeSet <BlockIndex> ();
		TreeSet <BlockIndex> lastFound = new TreeSet <BlockIndex> ();
		
		if (!blocksToPump.containsKey(y)) {
			blocksToPump.put(y, new LinkedList <BlockIndex> ());
		}
		
		LinkedList<BlockIndex> pumpList = blocksToPump.get(y);
		
		addToPumpIfOil(new BlockIndex(x, y, z), markedBlocks, lastFound, pumpList);
		
		while (lastFound.size() > 0) {
			TreeSet <BlockIndex> visitIteration = new TreeSet<BlockIndex> (lastFound);
			lastFound.clear();
								
			for (BlockIndex index : visitIteration) {								
				addToPumpIfOil(new BlockIndex(index.i + 1, index.j, index.k), markedBlocks, lastFound, pumpList);
				addToPumpIfOil(new BlockIndex(index.i - 1, index.j, index.k), markedBlocks, lastFound, pumpList);
				addToPumpIfOil(new BlockIndex(index.i, index.j, index.k + 1), markedBlocks, lastFound, pumpList);
				addToPumpIfOil(new BlockIndex(index.i, index.j, index.k - 1), markedBlocks, lastFound, pumpList);
				
				if (!blocksToPump.containsKey(index.j + 1)) {
					blocksToPump.put(index.j + 1, new LinkedList <BlockIndex> ());
				}
				
				pumpList = blocksToPump.get(index.j + 1);
				
				addToPumpIfOil(new BlockIndex(index.i, index.j + 1, index.k), markedBlocks, lastFound, pumpList);
			}
		}
	}
	
	public void addToPumpIfOil(BlockIndex index,
			TreeSet<BlockIndex> markedBlocks, TreeSet<BlockIndex> lastFound,
			LinkedList<BlockIndex> pumpList) {			
		
		if (!markedBlocks.contains(index)) {
			markedBlocks.add(index);
					
			if (isPumpableOil (index)) {					
				pumpList.push(index);	
			}
			
			if (isOil(index)) {
				lastFound.add(index);
			}			
		}
	}
	
	private boolean isPumpableOil(BlockIndex index) {
		return isOil(index)
				&& worldObj.getBlockMetadata(index.i, index.j, index.k) == 0;
	}
	
	private boolean isOil(BlockIndex index) {
		return index != null
				&& (worldObj.getBlockId(index.i, index.j, index.k) == BuildCraftEnergy.oilStill.blockID || worldObj
						.getBlockId(index.i, index.j, index.k) == BuildCraftEnergy.oilMoving.blockID);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		super.readFromNBT(nbttagcompound);
		
		internalLiquid = nbttagcompound.getInteger("internalLiquid");
    	aimY = nbttagcompound.getInteger("aimY");
    	
    	tubeY = nbttagcompound.getFloat("tubeY");
    	
    	BuildCraftCore.powerFramework.loadPowerProvider(this, nbttagcompound);
    	powerProvider.configure(20, 10, 10, 10, 100);
		
    }

	@Override
    public void writeToNBT(NBTTagCompound nbttagcompound) {
    	super.writeToNBT(nbttagcompound);
    	
    	BuildCraftCore.powerFramework.savePowerProvider(this, nbttagcompound);
    	
    	nbttagcompound.setInteger("internalLiquid", internalLiquid);
    	nbttagcompound.setInteger("aimY", aimY);
    	
    	if (tube != null) {
    		nbttagcompound.setFloat("tubeY", (float) tube.posY);
    	} else {
    		nbttagcompound.setFloat("tubeY", (float) yCoord);
    	}
    }

	@Override
	public boolean isActive() {
		return true;
	}

	@Override
	public void setPowerProvider(PowerProvider provider) {
		powerProvider = provider;
	}

	@Override
	public PowerProvider getPowerProvider() {
		return powerProvider;
	}

	@Override
	public void doWork() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleDescriptionPacket(Packet230ModLoader packet) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleUpdatePacket(Packet230ModLoader packet) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Packet230ModLoader getUpdatePacket() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Packet getDescriptionPacket() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
}
