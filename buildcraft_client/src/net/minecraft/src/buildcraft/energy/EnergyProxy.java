/** 
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src.buildcraft.energy;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.ModLoader;
import net.minecraft.src.buildcraft.api.APIProxy;

public class EnergyProxy {

	public static void displayGUISteamEngine(EntityPlayer entityplayer,
			TileEngine tile) {
		if (!APIProxy.isClient(APIProxy.getWorld())) {
			ModLoader.getMinecraftInstance().displayGuiScreen(
					new GuiSteamEngine(entityplayer.inventory, tile));
		}
	}
	
	public static void displayGUICombustionEngine(EntityPlayer entityplayer,
			TileEngine tile) {
		if (!APIProxy.isClient(APIProxy.getWorld())) {
			ModLoader.getMinecraftInstance().displayGuiScreen(
					new GuiCombustionEngine(entityplayer.inventory, tile));
		}
	}
	
}
