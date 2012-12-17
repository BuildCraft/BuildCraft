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
	private int heat = 0;;

	public EnergyPulser(IPowerReceptor receptor) {
		powerReceptor = receptor;
	}

	public void update() {
		if (powerReceptor == null)
			return;

		// Set current pulse speed
		pulseSpeed = getPulseSpeed();

		// Check if we are already running
		if (progressPart != 0) {

			progress += pulseSpeed;

			if (progress > 0.5 && progressPart == 1) {
				progressPart = 2;
				// Give off energy pulse!
				powerReceptor.getPowerProvider().receiveEnergy(1, ForgeDirection.WEST);

				// Heat up
				if (heat < maxHeat) {
					heat++;
				}

			} else if (progress >= 1) {
				progress = 0;
				progressPart = 0;
			}
		} else if (isActive) {
			progressPart = 1;
		} else {
			// Cool down when deactivated
			heat--;
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
		if (heat / (double) maxHeat * 100.0 <= 25.0)
			return 0.01F;
		else if (heat / (double) maxHeat * 100.0 <= 50.0)
			return 0.02F;
		else if (heat / (double) maxHeat * 100.0 <= 75.0)
			return 0.04F;
		else
			return 0.08F;
	}

	public void writeToNBT(NBTTagCompound nbttagcompound) {
		nbttagcompound.setInteger("Heat", heat);
		nbttagcompound.setBoolean("IsActive", isActive);
		nbttagcompound.setShort("ProgressPart", (short) progressPart);
		nbttagcompound.setFloat("Progress", progress);
	}

	public void readFromNBT(NBTTagCompound nbttagcompound) {
		heat = nbttagcompound.getInteger("Heat");
		isActive = nbttagcompound.getBoolean("IsActive");
		progressPart = nbttagcompound.getShort("ProgressPart");
		progress = nbttagcompound.getFloat("Progress");
	}
}
