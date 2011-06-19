package net.minecraft.src.buildcraft.builders;

import net.minecraft.src.Block;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.World;

public class BuildersProxy {

	public static void displayGUITemplate(EntityPlayer entityplayer,
			TileTemplate tile) {

	}
	
	public static void displayGUIBuilder(EntityPlayer entityplayer,
			TileBuilder tile) {
	
	}

	public static void displayGUIFiller(EntityPlayer entityplayer,
			TileFiller tile) {		
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
