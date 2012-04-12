/** 
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src;

import net.minecraft.src.buildcraft.core.DefaultProps;
import net.minecraft.src.forge.NetworkMod;

public class mod_BuildCraftFactory extends NetworkMod {		
	
	public static mod_BuildCraftFactory instance;
	
	public mod_BuildCraftFactory() {
		instance = this;
	}

	@Override
	public void modsLoaded () {		
		super.modsLoaded();
		
		BuildCraftFactory.initialize();
		
		instance = this;
	}
		
	@Override
	public String getVersion() {
		return DefaultProps.VERSION;
	}

	@Override
	public void load() {
		BuildCraftFactory.load();
	}
	
	@Override public boolean clientSideRequired() { return true; }
	@Override public boolean serverSideRequired() { return false; }

}
