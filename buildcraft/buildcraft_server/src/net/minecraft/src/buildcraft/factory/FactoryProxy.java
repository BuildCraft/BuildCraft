package net.minecraft.src.buildcraft.factory;

import net.minecraft.src.BuildCraftFactory;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.ModLoader;
import net.minecraft.src.World;

public class FactoryProxy {

	public static void displayGUIAutoCrafting (World world, EntityPlayer entityplayer, int i, int j, int k) {
		TileAutoWorkbench tile = (TileAutoWorkbench) world.getBlockTileEntity(i, j, k);
		
		ModLoader.OpenGUI(entityplayer, BuildCraftFactory.craftingGUI,
				tile, new ContainerAutoWorkbench(entityplayer.inventory, world, tile));
	}
	
}
