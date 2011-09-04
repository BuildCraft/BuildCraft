package net.minecraft.src.buildcraft.transport;

import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import net.minecraft.src.buildcraft.core.ILiquidContainer;

public class PipeTransport {

	public int xCoord;
	public int yCoord;
	public int zCoord;
	public World worldObj;
	public TileGenericPipe container;
	
	public void setPosition (int xCoord, int yCoord, int zCoord) {
		this.xCoord = xCoord;
		this.yCoord = yCoord;
		this.zCoord = zCoord;
	}
	
	public void setWorld (World worldObj) {
		this.worldObj = worldObj;
	}
	
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		
	}
	
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		  
	}

	public void updateEntity() {
		
	}

	public void setTile(TileGenericPipe tile) {
		this.container = tile;		
	}

	public boolean isPipeConnected(TileEntity tile) {
		return true;
	}
	
}
