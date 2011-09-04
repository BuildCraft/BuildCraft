package net.minecraft.src.buildcraft.transport;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.World;

public class PipeLogic {

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

	public boolean blockActivated(EntityPlayer entityplayer) {

		return false;
	}
	
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		
	}

	public void readFromNBT(NBTTagCompound nbttagcompound) {
		
	}

	public void setTile(TileGenericPipe tile) {
		this.container = tile;
	}

}
