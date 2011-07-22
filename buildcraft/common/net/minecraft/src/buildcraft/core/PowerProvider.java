package net.minecraft.src.buildcraft.core;

import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.TileEntity;

public abstract class PowerProvider {

	public int latency;
	public int minEnergyReceived;
	public int maxEnergyReceived;
	public int maxEnergyStored;
	public int minActivationEnergy;	
	public int energyStored = 0;
	
    private int powerLoss = 1;
	private int powerLossRegularity = 80;
	
	public SafeTimeTracker timeTracker = new SafeTimeTracker();
	public SafeTimeTracker energyLossTracker = new SafeTimeTracker();
	
	public void configure(int latency, int minEnergyReceived,
			int maxEnergyReceived, int minActivationEnergy, int maxStoredEnergy) {
		this.latency = latency;
		this.minEnergyReceived = minEnergyReceived;
		this.maxEnergyReceived = maxEnergyReceived;
		this.maxEnergyStored = maxStoredEnergy;
		this.minActivationEnergy = minActivationEnergy;
	}
	
	public void configurePowerPerdition(int powerLoss, int powerLossRegularity) {
		this.powerLoss = powerLoss;
		this.powerLossRegularity = powerLossRegularity;
	}
	
	public final boolean update (IPowerReceptor receptor) {
		if (!preConditions(receptor)) {
			return false;			
		}
		
		TileEntity tile = (TileEntity) receptor;
		boolean result = false;
		
		if (energyStored >= minActivationEnergy) {
			if (latency == 0) {
				receptor.doWork();
				result = true;
			} else {		
				if (timeTracker.markTimeIfDelay(tile.worldObj, latency)) {
					receptor.doWork();
					result = true;
				}
			}
		}
		
		if (energyLossTracker.markTimeIfDelay(tile.worldObj,
				powerLossRegularity)) {
			energyStored -= powerLoss;
			if (energyStored < 0) {
				energyStored = 0;
			}
		}		
		
		return result;		
	}
	
	public boolean preConditions (IPowerReceptor receptor) {
		return true;
	}
	
	public int useEnergy (int min, int max) {
		int result = 0;
		
		if (energyStored >= min) {
			if (energyStored <= max) {
				result = energyStored;
				energyStored = 0;
			} else {
				result = max;
				energyStored -= max;
			}
		}
		
		return result;
	}
	
	
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		latency = nbttagcompound.getInteger("latency");
		minEnergyReceived = nbttagcompound.getInteger("minEnergyReceived");
		maxEnergyReceived = nbttagcompound.getInteger("maxEnergyReceived");
		maxEnergyStored = nbttagcompound.getInteger("maxStoreEnergy");
		minActivationEnergy = nbttagcompound.getInteger("minActivationEnergy");	
		energyStored = nbttagcompound.getInteger("storedEnergy");		
	}
	
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		nbttagcompound.setInteger("latency", latency);
		nbttagcompound.setInteger("minEnergyReceived", minEnergyReceived);
		nbttagcompound.setInteger("maxEnergyReceived", maxEnergyReceived);
		nbttagcompound.setInteger("maxStoreEnergy", maxEnergyStored);
		nbttagcompound.setInteger("minActivationEnergy", minActivationEnergy);
		nbttagcompound.setInteger("storedEnergy", energyStored);
	}
	
	public void receiveEnergy (int quantity) {
		energyStored += quantity;
		
		if (energyStored > maxEnergyStored) {
			energyStored = maxEnergyStored;
		}
	}
}
