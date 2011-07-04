package net.minecraft.src.buildcraft.builders;

import net.minecraft.src.Block;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.ModLoader;
import net.minecraft.src.World;
import net.minecraft.src.buildcraft.core.PacketIds;
import net.minecraft.src.buildcraft.core.Utils;

public class BuildersProxy {

	public static void displayGUITemplate(EntityPlayer entityplayer,
			TileTemplate tile) {
		ModLoader.OpenGUI(entityplayer, Utils.packetIdToInt(PacketIds.TemplateGUI),
				tile, new CraftingTemplate(entityplayer.inventory, tile));
	}
	
	public static void displayGUIBuilder(EntityPlayer entityplayer,
			TileBuilder tile) {
		ModLoader.OpenGUI(entityplayer, Utils.packetIdToInt(PacketIds.BuilderGUI),
				tile, new CraftingBuilder(entityplayer.inventory, tile));
	}

	public static void displayGUIFiller(EntityPlayer entityplayer,
			TileFiller tile) {		
		
		ModLoader.OpenGUI(entityplayer, Utils.packetIdToInt(PacketIds.FillerGUI),
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
