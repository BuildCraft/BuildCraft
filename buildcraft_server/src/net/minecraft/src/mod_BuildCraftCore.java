/** 
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src;

import java.util.Date;

import net.minecraft.server.MinecraftServer;
import net.minecraft.src.buildcraft.core.ClassMapping;

public class mod_BuildCraftCore extends BaseModMp {	
	
	public static mod_BuildCraftCore instance;
	
	public mod_BuildCraftCore () {
		instance = this;
	}
	
	BuildCraftCore proxy = new BuildCraftCore();
		
	public static void initialize () {
		BuildCraftCore.initialize ();
	}
		
	@Override
	public void modsLoaded () {
		super.modsLoaded();
		mod_BuildCraftCore.initialize();
		BuildCraftCore.initializeModel(this);
		ModLoader.setInGameHook(this, true, true);
	}
	
	@Override
	public String getVersion() {
		return version ();
	}
	
	public static String version() {
		return "2.2.13";
	}
	
	long lastReport = 0;

	@Override
	public void onTickInGame(MinecraftServer minecraftserver) {
		if (BuildCraftCore.trackNetworkUsage) {			
			Date d = new Date();

			if (d.getTime() - lastReport > 10000) {
				lastReport = d.getTime();
				int bytes = ClassMapping.report();
				System.out.println ("BuildCraft badwith = " + (bytes / 10) + " bytes / second");
				System.out.println ();
			}			
		}
	}

	@Override
	public void load() {
		// TODO Auto-generated method stub
		
	}
}
