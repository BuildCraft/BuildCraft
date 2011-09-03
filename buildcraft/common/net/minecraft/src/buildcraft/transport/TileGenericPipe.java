package net.minecraft.src.buildcraft.transport;

import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.TileEntity;

public class TileGenericPipe extends TileEntity {
	public Pipe pipe;

	public void writeToNBT(NBTTagCompound nbttagcompound) {
		super.writeToNBT(nbttagcompound);

		if (pipe != null) {		
			nbttagcompound.setInteger("pipeId", pipe.itemID);
		}
	}

	public void readFromNBT(NBTTagCompound nbttagcompound) {
		super.readFromNBT(nbttagcompound);
		
		pipe = BlockGenericPipe.createPipe(xCoord, yCoord, zCoord, nbttagcompound.getInteger("pipeId"));
	}
}
