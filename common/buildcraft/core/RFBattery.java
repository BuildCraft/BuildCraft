package buildcraft.core;

import net.minecraft.nbt.NBTTagCompound;

import cofh.api.energy.IEnergyStorage;

public class RFBattery implements IEnergyStorage {
	private int energy, maxEnergy, maxReceive, maxExtract;

	public RFBattery(int maxEnergy, int maxReceive, int maxExtract) {
		this.maxEnergy = maxEnergy;
		this.maxReceive = maxReceive;
		this.maxExtract = maxExtract;
	}

	public void readFromNBT(NBTTagCompound tag) {
		if (!(tag.hasKey("battery"))) {
			return;
		}

		NBTTagCompound battery = tag.getCompoundTag("battery");
		this.energy = battery.getInteger("energy");
		this.maxEnergy = battery.getInteger("maxEnergy");
		this.maxReceive = battery.getInteger("maxReceive");
		this.maxExtract = battery.getInteger("maxExtract");
	}

	public void writeToNBT(NBTTagCompound tag) {
		NBTTagCompound battery = new NBTTagCompound();
		battery.setInteger("energy", this.energy);
		battery.setInteger("maxEnergy", this.maxEnergy);
		battery.setInteger("maxReceive", this.maxReceive);
		battery.setInteger("maxExtract", this.maxExtract);
	}

	public int addEnergy(int minReceive, int maxReceive, boolean simulate) {
		int amountReceived = Math.min(maxReceive, maxEnergy - energy);

		if (amountReceived < minReceive) {
			return 0;
		}

		if (!simulate) {
			energy += amountReceived;
		}

		return amountReceived;
	}

	public int useEnergy(int minExtract, int maxExtract, boolean simulate) {
		int amountExtracted = Math.min(maxExtract, energy);

		if (amountExtracted < minExtract) {
			return 0;
		}

		if (!simulate) {
			energy -= amountExtracted;
		}

		return amountExtracted;
	}

	@Override
	public int receiveEnergy(int maxReceive, boolean simulate) {
		return addEnergy(0, Math.min(maxReceive, this.maxReceive), simulate);
	}

	@Override
	public int extractEnergy(int maxExtract, boolean simulate) {
		return useEnergy(0, Math.min(maxExtract, this.maxExtract), simulate);
	}

	@Override
	public int getEnergyStored() {
		return energy;
	}

	@Override
	public int getMaxEnergyStored() {
		return maxEnergy;
	}

	public int getMaxEnergyReceive() {
		return maxReceive;
	}

	public int getMaxEnergyExtract() {
		return maxExtract;
	}

	public void setEnergy(int iEnergy) {
		energy = iEnergy;

		if (energy < 0) {
			energy = 0;
		} else if (energy > maxEnergy) {
			energy = maxEnergy;
		}
	}
}
