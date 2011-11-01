/** 
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src;

public class mod_BuildCraftCore extends BaseModMp {	
	
	public static mod_BuildCraftCore instance;
	
	public mod_BuildCraftCore () {
		instance = this;
	}
	
	BuildCraftCore proxy = new BuildCraftCore();
		
	public static void initialize () {
		BuildCraftCore.initialize ();
	}
		
	public void ModsLoaded () {
		mod_BuildCraftCore.initialize();
		BuildCraftCore.initializeModel(this);
	}
	
	@Override
	public String Version() {
		return version ();
	}
	
	public static String version() {
		return "2.2.5";
	}
}
