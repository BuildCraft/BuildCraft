package net.minecraft.src.buildcraft.core;

import net.minecraft.src.BuildCraftCore;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.TileEntity;

public class RedstonePowerProvider extends PowerProvider {
	
	private boolean lastPower = false;
		
	@Override
	public void update(IPowerReceptor receptor) {
		TileEntity tile = (TileEntity) receptor;
		
		boolean currentPower = tile.worldObj.isBlockIndirectlyGettingPowered(
				tile.xCoord, tile.yCoord, tile.zCoord);
		
		if (BuildCraftCore.continuousCurrentModel) {
			if (currentPower) {
				workIfDelay(receptor);
			}
		} else {			
			if (currentPower != lastPower) {
				lastPower = currentPower;

				if (currentPower) {
					workIfDelay(receptor);
				}
			}
		}
	}

	public int useEnergy (int min, int max) {		
		return min;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		super.readFromNBT(nbttagcompound);
		lastPower = nbttagcompound.getBoolean("lastPower");
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		super.readFromNBT(nbttagcompound);
		nbttagcompound.setBoolean("lastPower", lastPower);
	}

}
