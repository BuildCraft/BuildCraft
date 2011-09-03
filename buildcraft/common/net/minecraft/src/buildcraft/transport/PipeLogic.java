package net.minecraft.src.buildcraft.transport;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.World;

public class PipeLogic {

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

	public boolean blockActivated(World world, int i, int j, int k,
			EntityPlayer entityplayer) {

		return false;
	}
	
}
