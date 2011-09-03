package net.minecraft.src.buildcraft.transport;

import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.TileEntity;
import net.minecraft.src.buildcraft.api.IPowerReceptor;
import net.minecraft.src.buildcraft.api.PowerProvider;
import net.minecraft.src.buildcraft.core.BlockIndex;

public class TileGenericPipe extends TileEntity implements IPowerReceptor {
	public Pipe pipe;

	public void writeToNBT(NBTTagCompound nbttagcompound) {
		super.writeToNBT(nbttagcompound);

		if (pipe != null) {		
			nbttagcompound.setInteger("pipeId", pipe.itemID);
			pipe.writeToNBT(nbttagcompound);
		}
	}

	public void readFromNBT(NBTTagCompound nbttagcompound) {
		super.readFromNBT(nbttagcompound);
		
		pipe = BlockGenericPipe.createPipe(xCoord, yCoord, zCoord, nbttagcompound.getInteger("pipeId"));
		pipe.readFromNBT(nbttagcompound);	
	}
		
	@Override
	public void validate () {
		super.validate();
		
		if (pipe == null) {
			pipe = BlockGenericPipe.pipeBuffer.get(new BlockIndex(xCoord, yCoord, zCoord));
		}
		
		pipe.setWorld(worldObj);
	}
	
	@Override
	public void updateEntity () {
		PowerProvider provider = getPowerProvider();
		
		if (provider != null) {			
			provider.update(this);
		}
		
		pipe.updateEntity ();
	
	}

	@Override
	public void setPowerProvider(PowerProvider provider) {		
		if (pipe instanceof IPowerReceptor) {
			((IPowerReceptor) pipe).setPowerProvider(provider);
		}
		
	}

	@Override
	public PowerProvider getPowerProvider() {
		if (pipe instanceof IPowerReceptor) {
			return ((IPowerReceptor) pipe).getPowerProvider();
		} else {
			return null;
		}
	}

	@Override
	public void doWork() {
		if (pipe instanceof IPowerReceptor) {
			((IPowerReceptor) pipe).doWork();
		}		
	}
}
