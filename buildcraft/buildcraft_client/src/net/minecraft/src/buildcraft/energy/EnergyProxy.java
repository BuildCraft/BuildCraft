/** 
 * BuildCraft is open-source. It is distributed under the terms of the 
 * BuildCraft Open Source License. It grants rights to read, modify, compile
 * or run the code. It does *NOT* grant the right to redistribute this software
 * or its modifications in any form, binary or source, except if expressively
 * granted by the copyright holder.
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
