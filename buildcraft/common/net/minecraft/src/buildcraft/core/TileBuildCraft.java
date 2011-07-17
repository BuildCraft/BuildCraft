package net.minecraft.src.buildcraft.core;

import net.minecraft.src.TileEntity;

public abstract class TileBuildCraft extends TileEntity {

	private boolean init = false;
	
	@Override
	public void updateEntity () {
		if (!init) {
			initialize();
			init = true;
		}
		
		if (this instanceof IPowerReceptor) {
			IPowerReceptor receptor = ((IPowerReceptor) this);
			
			receptor.getPowerProvider().update(receptor);
		}
	}
	
	public void initialize () {
		
	}
	
}
