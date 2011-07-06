package net.minecraft.src;

import net.minecraft.src.buildcraft.energy.TileEngine;

public class mod_BuildCraftEnergy extends BaseModMp {

	public static mod_BuildCraftEnergy instance;
	
	public void ModsLoaded () {
		super.ModsLoaded();
		BuildCraftEnergy.ModsLoaded();	
		
		ModLoader.RegisterTileEntity(TileEngine.class,
				"net.minecraft.src.buildcraft.energy.Engine");
		
		instance = this;
	}
	
	@Override
	public String Version() {
		return "1.7.2.1";
	}
}
