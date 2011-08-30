package net.minecraft.src;

import java.util.Random;

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
		return "2.1.1";
	}
	
    public void GenerateSurface(World world, Random random, int i, int j) {
    	BuildCraftEnergy.generateSurface (world, random, i, j);
    }
}
