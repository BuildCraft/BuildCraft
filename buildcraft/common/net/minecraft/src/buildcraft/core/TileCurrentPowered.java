package net.minecraft.src.buildcraft.core;

import net.minecraft.src.BuildCraftCore;
import net.minecraft.src.TileEntity;

public abstract class TileCurrentPowered extends TileEntity {

	public boolean continuousCurrentModel = BuildCraftCore.continuousCurrentModel;
	
	public double latency = 1;	
	boolean lastPower;
	boolean init = false;
	
	public double lastWorkTime = 0;
	
	public TileCurrentPowered () {

	}
			
	public void updateEntity () {
		super.updateEntity();
		
		if (!init) {
			init = true;
			
			initialize ();
		}
		
		if (continuousCurrentModel
				&& worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord,
						zCoord)) {
			tryWork();
		}
	}
	
	public final void checkPower () {
		boolean power = worldObj.isBlockIndirectlyGettingPowered(xCoord,
				yCoord, zCoord);
		
		if (power != lastPower) {
			lastPower = power;
			
			tryWork();
		}
	}
	
	public void tryWork () {
		if (worldObj.getWorldTime() - lastWorkTime > latency) {
			lastWorkTime = worldObj.getWorldTime();
			
			doWork ();
		}
	}
	
	protected abstract void doWork ();
	
	public void initialize () {
		
	}
	
}
