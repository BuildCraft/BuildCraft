package net.minecraft.src.buildcraft.energy;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.ModLoader;
import net.minecraft.src.buildcraft.core.PacketIds;
import net.minecraft.src.buildcraft.core.Utils;

public class EnergyProxy {

	public static void displayGUISteamEngine(EntityPlayer entityplayer,
			TileEngine tile) {

	}

	public static void displayGUICombustionEngine(EntityPlayer entityplayer,
			TileEngine tile) {
		ModLoader.OpenGUI(entityplayer, Utils.packetIdToInt(PacketIds.EngineCombustionGUI),
				tile, new ContainerCombustionEngine(entityplayer.inventory, tile));
	}
	
}
