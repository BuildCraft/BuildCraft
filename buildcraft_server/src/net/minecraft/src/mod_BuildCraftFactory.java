/** 
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src;

public class mod_BuildCraftFactory extends BaseModMp {		
	
	public static mod_BuildCraftFactory instance;
	
	public void ModsLoaded () {		
		super.ModsLoaded();
		
		BuildCraftFactory.initialize();
		
		instance = this;
	}
		
	@Override
	public String Version() {
		return "2.2.5";
	}
}
