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
import net.minecraft.src.buildcraft.core.DefaultProps;
import net.minecraft.src.forge.NetworkMod;

public class mod_BuildCraftCore extends NetworkMod {	
	
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
		return DefaultProps.VERSION;
	}
	
	long lastReport = 0;

	@Override
	public boolean onTickInGame(MinecraftServer minecraftserver) {
		if (BuildCraftCore.trackNetworkUsage) {			
			Date d = new Date();

			if (d.getTime() - lastReport > 10000) {
				lastReport = d.getTime();
				int bytes = ClassMapping.report();
				System.out.println ("BuildCraft badwith = " + (bytes / 10) + " bytes / second");
				System.out.println ();
			}			
		}
		
		return true;
	}

	@Override
	public void load() {
		BuildCraftCore.load();		
	}
	
	@Override public boolean clientSideRequired() { return true; }
	@Override public boolean serverSideRequired() { return false; }
	

}
