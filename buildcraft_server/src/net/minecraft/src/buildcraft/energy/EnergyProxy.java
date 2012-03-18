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
import net.minecraft.src.buildcraft.core.PacketIds;
import net.minecraft.src.buildcraft.core.Utils;

public class EnergyProxy {

	public static void displayGUISteamEngine(EntityPlayer entityplayer,
			TileEngine tile) {
		ModLoader.openGUI(entityplayer, Utils.packetIdToInt(PacketIds.EngineSteamGUI),
				tile, new ContainerEngine(entityplayer.inventory, tile));
	}

	public static void displayGUICombustionEngine(EntityPlayer entityplayer,
			TileEngine tile) {
		ModLoader.openGUI(entityplayer, Utils.packetIdToInt(PacketIds.EngineCombustionGUI),
				tile, new ContainerEngine(entityplayer.inventory, tile));
	}
	
}
