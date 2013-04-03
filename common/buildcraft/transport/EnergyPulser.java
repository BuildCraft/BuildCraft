package buildcraft.transport;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.ForgeDirection;
import buildcraft.api.power.IPowerReceptor;

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
		if (powerReceptor == null || !isActive || tick++ % 10 != 0)
			return;

		if (!singlePulse || !hasPulsed) {
			powerReceptor.getPowerProvider().receiveEnergy(Math.min(1 << (pulseCount - 1), 64), ForgeDirection.WEST);
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

	private float getPulseSpeed() {
		return 0.1F;
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
