package net.minecraft.src.buildcraft.energy;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.ModLoader;
import net.minecraft.src.buildcraft.core.PacketIds;

public class EnergyProxy {

	public static void displayGUISteamEngine(EntityPlayer entityplayer,
			TileEngine tile) {

	}

	public static void displayGUICombustionEngine(EntityPlayer entityplayer,
			TileEngine tile) {
		ModLoader.OpenGUI(entityplayer, PacketIds.TemplateGUI.ordinal(),
				tile, new ContainerCombustionEngine(entityplayer.inventory, tile));
	}
	
}
