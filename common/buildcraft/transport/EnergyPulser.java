package buildcraft.transport;

import buildcraft.api.power.IPowerReceptor;
import buildcraft.api.power.PowerHandler.Type;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.ForgeDirection;

public class EnergyPulser {

	private final IPowerReceptor powerReceptor;

	private boolean isActive;
	private boolean singlePulse;
	private boolean hasPulsed;
	private int pulseCount;
	private int tick;

	public EnergyPulser(IPowerReceptor receptor) {
		powerReceptor = receptor;
	}

	public void update() {
	    if (!isActive && hasPulsed)
	        hasPulsed = false;

		if (powerReceptor == null || !isActive || tick++ % 10 != 0)
			return;

		if (!singlePulse || !hasPulsed) {
			powerReceptor.getPowerReceiver(null).receiveEnergy(Type.GATE, Math.min(1 << (pulseCount - 1), 64) *1.01f, ForgeDirection.WEST);
			hasPulsed = true;
		}
	}

	public void enableSinglePulse(int count) {
		singlePulse = true;
		isActive = true;
		pulseCount = count;
	}

	public void enablePulse(int count) {
		isActive = true;
		singlePulse = false;
		pulseCount = count;
	}

	public void disablePulse() {
		if (!isActive) {
			hasPulsed = false;
		}
		isActive = false;
		pulseCount = 0;
	}

	public boolean isActive() {
		return isActive;
	}

	public void writeToNBT(NBTTagCompound nbttagcompound) {
		nbttagcompound.setBoolean("SinglePulse", singlePulse);
		nbttagcompound.setBoolean("IsActive", isActive);
		nbttagcompound.setBoolean("hasPulsed", hasPulsed);
		nbttagcompound.setInteger("pulseCount", pulseCount);
		nbttagcompound.setInteger("tick", tick);
	}

	public void readFromNBT(NBTTagCompound nbttagcompound) {
		isActive = nbttagcompound.getBoolean("IsActive");
		singlePulse = nbttagcompound.getBoolean("SinglePulse");
		hasPulsed = nbttagcompound.getBoolean("hasPulsed");
		pulseCount = nbttagcompound.getInteger("pulseCount");
		tick = nbttagcompound.getInteger("tick");
	}
}
