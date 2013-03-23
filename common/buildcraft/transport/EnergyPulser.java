package buildcraft.transport;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagInt;
import net.minecraftforge.common.ForgeDirection;
import buildcraft.api.power.IPowerReceptor;

public class EnergyPulser {

	private IPowerReceptor powerReceptor;

	private boolean isActive = false;
	private int progress = 0;

	public EnergyPulser(IPowerReceptor receptor) {
		powerReceptor = receptor;
	}

	public void update() {
		if (powerReceptor == null)
			return;

		if (isActive) {
			progress = (progress + 1) % 10;

			if (progress == 5)
				powerReceptor.getPowerProvider().receiveEnergy(1, ForgeDirection.WEST);
		} else {
			progress = 0;
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

	public void writeToNBT(NBTTagCompound nbttagcompound) {
		nbttagcompound.setBoolean("IsActive", isActive);
		nbttagcompound.setInteger("Progress", progress);
	}

	public void readFromNBT(NBTTagCompound nbttagcompound) {
		isActive = nbttagcompound.getBoolean("IsActive");

		NBTBase tag = nbttagcompound.getTag("Progress");

		if (tag instanceof NBTTagInt) {
			progress = ((NBTTagInt) tag).data;
		} else if (tag instanceof NBTTagFloat) {
			// progress was a float in 3.4.3 and below
			progress = ((int) ((NBTTagFloat) tag).data) % 10;
		}
	}
}
