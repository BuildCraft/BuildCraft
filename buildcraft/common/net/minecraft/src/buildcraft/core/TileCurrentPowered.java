package net.minecraft.src.buildcraft.core;

import net.minecraft.src.BuildCraftCore;
import net.minecraft.src.TileEntity;

public abstract class TileCurrentPowered extends TileEntity {

	public boolean continuousCurrentModel = BuildCraftCore.continuousCurrentModel;
	
	public long latency = 1;	
	boolean lastPower;
	protected boolean init = false;
	
	public SafeTimeTracker workTracker = new SafeTimeTracker();
	
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
			
			if (power) {
				tryWork();
			}
		}
	}
	
	public void tryWork () {
		if (workTracker.markTimeIfDelay(worldObj, latency)) {			
			doWork ();
		}
	}
	
	protected abstract void doWork ();
	
	public void initialize () {
		
	}	
	
}
