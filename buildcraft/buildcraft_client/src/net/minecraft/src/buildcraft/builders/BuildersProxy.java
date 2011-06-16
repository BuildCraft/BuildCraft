package net.minecraft.src.buildcraft.builders;

import net.minecraft.src.Block;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.ModLoader;
import net.minecraft.src.World;
import net.minecraft.src.buildcraft.api.APIProxy;

public class BuildersProxy {

	public static void displayGUITemplate(EntityPlayer entityplayer,
			TileTemplate tile) {
		if (!APIProxy.isClient(APIProxy.getWorld())) {
			ModLoader.getMinecraftInstance().displayGuiScreen(
					new GuiTemplate(entityplayer.inventory, tile));
		}
	}

	public static void displayGUIBuilder(EntityPlayer entityplayer,
			TileBuilder tile) {
		if (!APIProxy.isClient(APIProxy.getWorld())) {
			ModLoader.getMinecraftInstance().displayGuiScreen(
					new GuiBuilder(entityplayer.inventory, tile));
		}
	}
	
	public static void displayGUIFiller(EntityPlayer entityplayer,
			TileFiller tile) {
		if (!APIProxy.isClient(APIProxy.getWorld())) {
			ModLoader.getMinecraftInstance().displayGuiScreen(
					new GuiFiller(entityplayer.inventory, tile));
		}
	}
	
	public static boolean canPlaceTorch (World w, int i, int j, int k) {
		Block block = Block.blocksList [w.getBlockId(i, j, k)];
		
		if (block == null || !block.renderAsNormalBlock()) {
			return false;
		} else {
			return true;
		}
	}

}
