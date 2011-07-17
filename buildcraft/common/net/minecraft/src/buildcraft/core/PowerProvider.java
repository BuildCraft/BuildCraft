package net.minecraft.src.buildcraft.core;

import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.TileEntity;

public abstract class PowerProvider {

	public int latency;
	public int minEnergyReceived;
	public int maxEnergyReceived;
	public int maxEnergyStored;
	
	private int energyStored = 0;
	private SafeTimeTracker timeTracker = new SafeTimeTracker();
	
	public void configure (int latency, int minEnergyReceived, int maxEnergyReceived, int maxStoredEnergy) {
		this.latency = latency;
		this.minEnergyReceived = minEnergyReceived;
		this.maxEnergyReceived = maxEnergyReceived;
		this.maxEnergyStored = maxStoredEnergy;
	}
	
	public final boolean workIfDelay (IPowerReceptor receptor) {
		if (latency == 0) {
			receptor.doWork();
			return true;
		} else {
			TileEntity tile = (TileEntity) receptor;
			
			if (timeTracker.markTimeIfDelay(tile.worldObj, latency)) {
				receptor.doWork();
				return true;
			}
		}
		
		return false;
	}
	
	public abstract void update (IPowerReceptor receptor);
	
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
		energyStored = nbttagcompound.getInteger("storedEnergy");		
	}
	
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		nbttagcompound.setInteger("latency", latency);
		nbttagcompound.setInteger("minEnergyReceived", minEnergyReceived);
		nbttagcompound.setInteger("maxEnergyReceived", maxEnergyReceived);
		nbttagcompound.setInteger("maxStoreEnergy", maxEnergyStored);
		nbttagcompound.setInteger("storedEnergy", energyStored);
	}
	
	public void receiveEnergy (int quantity) {
		energyStored += quantity;
		
		if (energyStored > maxEnergyStored) {
			energyStored = maxEnergyStored;
		}
	}
}
