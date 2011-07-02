package net.minecraft.src.buildcraft.energy;

import net.minecraft.src.ModLoader;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.TileEntity;
import net.minecraft.src.buildcraft.api.APIProxy;
import net.minecraft.src.buildcraft.api.Orientations;
import net.minecraft.src.buildcraft.api.Position;
import net.minecraft.src.buildcraft.core.CoreProxy;
import net.minecraft.src.buildcraft.core.IPowerReceptor;

public class TileEngine extends TileEntity implements IPowerReceptor {

	boolean init = false;
	
	EntityEngine entity;
	
	boolean lastPower = false;
	
	int progressPart = 0;

	public int orientation;
	
	public void switchPower () {
		boolean power = worldObj.isBlockGettingPowered(xCoord, yCoord, zCoord);
	
		if (power != lastPower)	{
			lastPower = power;
			
			if (power) {
				entity.addEnergy(1);
			}
		}
	}
	
	@Override
	public void updateEntity () {
		if (!init) {
			if (entity == null) {
				int kind = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);
				
				if (kind == 0) {
					entity = new EntityEngineWood(worldObj);
				} else if (kind == 1) {
					entity = new EntityEngineStone(worldObj);
				}
			}
			
			entity.orientation = Orientations.values()[orientation];
			
			entity.setPosition(xCoord, yCoord, zCoord);
			
			worldObj.entityJoinedWorld(entity);
			
			init = true;
		}
		
		if (progressPart != 0) {
			switch (entity.getEnergyStage()) {
			case Blue:
				entity.progress += 0.01;
				break;
			case Green:
				entity.progress += 0.02;
				break;
			case Yellow:
				entity.progress += 0.04;
				break;
			case Red:
				entity.progress += 0.1;
				break;
			}
			
			if (entity.progress > 0.5 && progressPart == 1) {
				progressPart = 2;
				
				Position pos = new Position(xCoord, yCoord, zCoord,
						entity.orientation);
				pos.moveForwards(1.0);
				TileEntity tile = worldObj.getBlockTileEntity((int) pos.x, (int) pos.y,
						(int) pos.z);
				
				if (tile instanceof IPowerReceptor) {
					IPowerReceptor receptor = (IPowerReceptor) tile;
					
					int minEnergy = receptor.minEnergyExpected();
					
					if (minEnergy != -1 && minEnergy <= entity.energy) {
						int energySent = receptor.maxEnergyExpected();
						
						if (entity.energy < energySent) {
							energySent = entity.energy;
						}
						
						entity.energy -= energySent;
						receptor.receiveEnergy(energySent);
					}
				}
			} else if (entity.progress >= 1) {
				entity.progress = 0;
				progressPart = 0;
			}
		} else {
			Position pos = new Position(xCoord, yCoord, zCoord,
					entity.orientation);
			pos.moveForwards(1.0);
			TileEntity tile = worldObj.getBlockTileEntity((int) pos.x, (int) pos.y,
					(int) pos.z);

			if (tile instanceof IPowerReceptor) {
				IPowerReceptor receptor = (IPowerReceptor) tile;
				int minEnergy = receptor.minEnergyExpected();

				if (minEnergy != -1 && minEnergy <= entity.energy) {
					progressPart = 1;										
				}
			}
		}
	}
	
	public void switchOrientation () {						
		for (int i = orientation + 1; i <= orientation + 6; ++i) {
			Orientations o = Orientations.values() [i % 6];
			
			Position pos = new Position (xCoord, yCoord, zCoord, o);
			
			pos.moveForwards(1);
			
			TileEntity tile = worldObj.getBlockTileEntity((int) pos.x, (int) pos.y,
					(int) pos.z);
			
			if (tile instanceof IPowerReceptor) {
				if (entity != null) {
					entity.orientation = o;	
				}				
				orientation = o.ordinal();
				break;
			}
		}
	}
	
	public void delete () {
		entity.setEntityDead();
	}
	
    public void readFromNBT(NBTTagCompound nbttagcompound)
    {
    	super.readFromNBT(nbttagcompound);
    	
		int kind = nbttagcompound.getInteger("kind");
		
		if (kind == 0) {
			entity = new EntityEngineWood(APIProxy.getWorld());
		} else if (kind == 1) {
			entity = new EntityEngineStone(APIProxy.getWorld());
		}
		
		orientation = nbttagcompound.getInteger("orientation");
    	entity.progress = nbttagcompound.getFloat("progress");
    	entity.energy = nbttagcompound.getInteger("energy");
    	entity.orientation = Orientations.values()[orientation];
    }
    

    public void writeToNBT(NBTTagCompound nbttagcompound) {
    	super.writeToNBT(nbttagcompound);
    	
		nbttagcompound.setInteger("kind",
				worldObj.getBlockMetadata(xCoord, yCoord, zCoord));
		nbttagcompound.setInteger("orientation", orientation);
    	nbttagcompound.setFloat("progress", entity.progress);
    	nbttagcompound.setInteger("energy", entity.energy);
    }

	@Override
	public int minEnergyExpected() {
		return 5;
	}

	@Override
	public int maxEnergyExpected() {
		return entity.maxEnergyReceived();
	}

	@Override
	public void receiveEnergy(int energy) {
		entity.addEnergy((int) (energy * 0.9));		
	}
	
}
