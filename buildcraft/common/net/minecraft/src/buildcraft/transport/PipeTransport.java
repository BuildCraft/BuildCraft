package net.minecraft.src.buildcraft.transport;

import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.World;

public class PipeTransport {

	public int xCoord;
	public int yCoord;
	public int zCoord;
	public World worldObj;
	
	public void initialize (int xCoord, int yCoord, int zCoord, World worldObj) {
		this.xCoord = xCoord;
		this.yCoord = yCoord;
		this.zCoord = zCoord;
		this.worldObj = worldObj;
	}
	
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		
	}
	
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		  
	}
	
}
