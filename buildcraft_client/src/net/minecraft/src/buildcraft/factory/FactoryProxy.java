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
import net.minecraft.src.buildcraft.api.APIProxy;
import net.minecraft.src.buildcraft.silicon.GuiAssemblyTable;

public class FactoryProxy {

	public static void displayGUIAutoCrafting (World world, EntityPlayer entityplayer, int i, int j, int k) {
		if (!APIProxy.isClient(world)) {
			ModLoader.getMinecraftInstance().displayGuiScreen(
					new GuiAutoCrafting(entityplayer.inventory, world,
							(TileAutoWorkbench) world.getBlockTileEntity(i, j, k)));
		}
	}
	
	public static void displayGUIAssemblyTable (World world, EntityPlayer entityplayer, int i, int j, int k) {
		if (!APIProxy.isClient(world)) {
			ModLoader.getMinecraftInstance().displayGuiScreen(
					new GuiAssemblyTable(entityplayer.inventory,
							(TileAssemblyTable) world.getBlockTileEntity(i, j, k)));
		}
	}
	
	public static void displayGUIRefinery (World world, EntityPlayer entityplayer, int i, int j, int k) {
		if (!APIProxy.isClient(world)) {
			ModLoader.getMinecraftInstance().displayGuiScreen(
					new GuiRefinery(entityplayer.inventory,
							(TileRefinery) world.getBlockTileEntity(i, j, k)));
		}
	}
	
}
