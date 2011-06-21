package net.minecraft.src.buildcraft.builders;

import net.minecraft.src.Block;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.ModLoader;
import net.minecraft.src.World;
import net.minecraft.src.buildcraft.core.PacketIds;

public class BuildersProxy {

	public static void displayGUITemplate(EntityPlayer entityplayer,
			TileTemplate tile) {
		ModLoader.OpenGUI(entityplayer, PacketIds.TemplateGUI.ordinal(),
				tile, new CraftingTemplate(entityplayer.inventory, tile));
	}
	
	public static void displayGUIBuilder(EntityPlayer entityplayer,
			TileBuilder tile) {
		ModLoader.OpenGUI(entityplayer, PacketIds.BuilderGUI.ordinal(),
				tile, new CraftingFiller(entityplayer.inventory, tile));
	}

	public static void displayGUIFiller(EntityPlayer entityplayer,
			TileFiller tile) {		
		
		ModLoader.OpenGUI(entityplayer, PacketIds.FillerGUI.ordinal(),
				tile, new CraftingFiller(entityplayer.inventory, tile));
	}

	public static boolean canPlaceTorch(World world, int i, int j, int k) {
		Block block = Block.blocksList [world.getBlockId(i, j, k)];
		
		if (block == null || !block.func_28025_b()) {
			return false;
		} else {
			return true;
		}
	}

}
