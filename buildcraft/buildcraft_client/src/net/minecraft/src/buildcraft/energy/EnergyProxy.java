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
	
}
