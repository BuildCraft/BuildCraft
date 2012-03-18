/** 
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src.buildcraft.factory;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.ModLoader;
import net.minecraft.src.World;
import net.minecraft.src.buildcraft.core.PacketIds;
import net.minecraft.src.buildcraft.core.Utils;

public class FactoryProxy {

	public static void displayGUIAutoCrafting (World world, EntityPlayer entityplayer, int i, int j, int k) {
		TileAutoWorkbench tile = (TileAutoWorkbench) world.getBlockTileEntity(i, j, k);
		
		ModLoader.openGUI(entityplayer, Utils.packetIdToInt(PacketIds.AutoCraftingGUI),
				tile, new ContainerAutoWorkbench(entityplayer.inventory, tile));
	}
	
}
