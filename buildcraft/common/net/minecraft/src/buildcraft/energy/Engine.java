/** 
 * BuildCraft is open-source. It is distributed under the terms of the 
 * BuildCraft Open Source License. It grants rights to read, modify, compile
 * or run the code. It does *NOT* grant the right to redistribute this software
 * or its modifications in any form, binary or source, except if expressively
 * granted by the copyright holder.
 */

package net.minecraft.src.buildcraft.energy;

import net.minecraft.src.ICrafting;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.buildcraft.api.Orientations;
import net.minecraft.src.buildcraft.api.TileNetworkData;

public abstract class Engine {

	public int maxEnergy;
	
	public @TileNetworkData float progress;	
	public @TileNetworkData Orientations orientation;	
	public @TileNetworkData int energy;	
	
	public int maxEnergyExtracted = 1;

	protected TileEngine tile;
	
	enum EnergyStage {
		Blue,
		Green,
		Yellow,
		Red,
		Explosion
	}
	
	public Engine (TileEngine tile) {
		this.tile = tile;
	}
			
	public EnergyStage getEnergyStage () {
		if (energy / (double) maxEnergy * 100.0 <= 25.0) {
			return EnergyStage.Blue;
		} else if (energy / (double) maxEnergy * 100.0 <= 50.0) {
		 	return EnergyStage.Green;
		}  else if (energy / (double) maxEnergy * 100.0 <= 75.0) {
			return EnergyStage.Yellow;
		} else if (energy / (double) maxEnergy * 100.0 <= 100.0) {
			return EnergyStage.Red;
		} else {
			return EnergyStage.Explosion;
		}
	}	
	
	public void update () {
		if (!tile.worldObj.isBlockIndirectlyGettingPowered(tile.xCoord,
				tile.yCoord, tile.zCoord)) {
			if (energy > 1) {
				energy -= 1;
			}
		}
	}
	
	public abstract String getTextureFile ();
	
	public abstract int explosionRange ();
	
	public abstract int maxEnergyReceived ();
	
	public abstract float getPistonSpeed ();
	
	public abstract boolean isBurning ();
	
	public abstract void delete();

	public void addEnergy (int addition) {
		energy += addition;
				
		if (getEnergyStage() == EnergyStage.Explosion) {
			tile.worldObj.createExplosion(null, tile.xCoord, tile.yCoord,
					tile.zCoord, explosionRange());
		}
		
		if (energy > maxEnergy) {
			energy = maxEnergy;
		}
	}
	
	public int extractEnergy (int min, int max, boolean doExtract) {				
		if (energy < min) {
			return 0;
		}
		
		int actualMax;
		
		if (max > maxEnergyExtracted) {
			actualMax = maxEnergyExtracted;
		} else {
			actualMax = max;
		}
		
		int extracted;
		
		if (energy >= actualMax) {
			extracted = actualMax;
			if (doExtract) {
				energy -= actualMax; 
			}
		} else {
			extracted = energy;
			if (doExtract) {
				energy = 0; 
			}
		}
		
		return extracted;
	}
	
	public abstract int getScaledBurnTime (int i);
	
	public abstract void burn ();
	
    public void readFromNBT(NBTTagCompound nbttagcompound) {
    	
    }
    
    public void writeToNBT(NBTTagCompound nbttagcompound) {
    	
    }

	public void getGUINetworkData(int i, int j) {
		
	}

	public void sendGUINetworkData(ContainerEngine containerEngine,
			ICrafting iCrafting) {
		
	}
}
