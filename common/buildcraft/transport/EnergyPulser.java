package buildcraft.transport;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.ForgeDirection;
import buildcraft.api.power.IPowerReceptor;

public class EnergyPulser {

	private IPowerReceptor powerReceptor;

	private boolean isActive = false;
	private float progress = 0;
	private int progressPart = 0;
	private float pulseSpeed = 0;
	private int maxHeat = 1000;

	public EnergyPulser(IPowerReceptor receptor) {
		powerReceptor = receptor;
	}

	public void update() {
		if (powerReceptor == null)
			return;

		// Check if we are already running
		if (progressPart != 0) {

			progress += getPulseSpeed();

			if (progress > 0.5 && progressPart == 1) {
				progressPart = 2;
				// Give off energy pulse!
				powerReceptor.getPowerProvider().receiveEnergy(1, ForgeDirection.WEST);

			} else if (progress >= 1) {
				progress = 0;
				progressPart = 0;
			}
		} else if (isActive) {
			progressPart = 1;
		}

	}

	public void enablePulse() {
		isActive = true;
	}

	public void disablePulse() {
		isActive = false;
	}

	public boolean isActive() {
		return isActive;
	}

	private float getPulseSpeed() {
		return 0.1F;
	}

	public void writeToNBT(NBTTagCompound nbttagcompound) {
		nbttagcompound.setBoolean("IsActive", isActive);
		nbttagcompound.setShort("ProgressPart", (short) progressPart);
		nbttagcompound.setFloat("Progress", progress);
	}

	public void readFromNBT(NBTTagCompound nbttagcompound) {
		isActive = nbttagcompound.getBoolean("IsActive");
		progressPart = nbttagcompound.getShort("ProgressPart");
		progress = nbttagcompound.getFloat("Progress");
	}
}
