package net.minecraft.src.buildcraft.factory;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.ModLoader;
import net.minecraft.src.World;

public class FactoryProxy {

	public static void displayGUIAutoCrafting (World world, EntityPlayer entityplayer, int i, int j, int k) {
    	ModLoader.getMinecraftInstance().displayGuiScreen(
				new GuiAutoCrafting(entityplayer.inventory, world, i, j, k));
	}
	
}
