// STUB(R.Chen): Forge IEnergyStorage — compile shim. TODO: replace with Team Reborn EnergyStorage.
package net.minecraftforge.energy;

public interface IEnergyStorage {
    int receiveEnergy(int maxReceive, boolean simulate);
    int extractEnergy(int maxExtract, boolean simulate);
    int getEnergyStored();
    int getMaxEnergyStored();
    boolean canExtract();
    boolean canReceive();
}
