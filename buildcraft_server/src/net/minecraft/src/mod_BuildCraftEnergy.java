/** 
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src;

import java.util.Random;

import net.minecraft.src.buildcraft.core.DefaultProps;
import net.minecraft.src.buildcraft.energy.TileEngine;
import net.minecraft.src.forge.NetworkMod;

public class mod_BuildCraftEnergy extends NetworkMod {

	public static mod_BuildCraftEnergy instance;
	
	public mod_BuildCraftEnergy() {
		instance = this;
	}
	
	@Override
	public void modsLoaded () {
		super.modsLoaded();
		BuildCraftEnergy.initialize();	
		
		ModLoader.registerTileEntity(TileEngine.class,
				"net.minecraft.src.buildcraft.energy.Engine");
		
		instance = this;
	}
	
	@Override
	public String getVersion() {
		return DefaultProps.VERSION;
	}
	
	@Override
    public void generateSurface(World world, Random random, int i, int j) {
    	BuildCraftEnergy.generateSurface (world, random, i, j);
    }

	@Override
	public void load() {
		BuildCraftEnergy.load();
	}
	
	@Override public boolean clientSideRequired() { return true; }
	@Override public boolean serverSideRequired() { return false; }

}
