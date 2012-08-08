package buildcraft.api.power;

import net.minecraft.src.NBTTagCompound;
import buildcraft.api.core.Orientations;
import buildcraft.api.core.SafeTimeTracker;

public interface IPowerProvider {

    int getLatency();

    int getMinEnergyReceived();

    int getMaxEnergyReceived();

    int getMaxEnergyStored();

    int getActivationEnergy();

    float getEnergyStored();

    void configure(int latency, int minEnergyReceived, int maxEnergyReceived, int minActivationEnergy, int maxStoredEnergy);

    void configurePowerPerdition(int powerLoss, int powerLossRegularity);

    boolean update(IPowerReceptor receptor);

    boolean preConditions(IPowerReceptor receptor);

    float useEnergy(float min, float max, boolean doUse);

    void readFromNBT(NBTTagCompound nbttagcompound);

    void writeToNBT(NBTTagCompound nbttagcompound);

    void receiveEnergy(float quantity, Orientations from);

    boolean isPowerSource(Orientations from);

    SafeTimeTracker getTimeTracker();

    float setEnergyStored(float quantity, boolean doSet);

    void setEnergyStored(float quantity);

    void addEnergy(float quantity);

}
