package net.minecraft.src.buildcraft.core;

import java.io.File;

import net.minecraft.client.Minecraft;
import net.minecraft.src.BaseModMp;
import net.minecraft.src.Block;
import net.minecraft.src.EntityItem;
import net.minecraft.src.ModLoader;
import net.minecraft.src.Packet230ModLoader;

public class CoreProxy {
	public static void addName(Object obj, String s) {
		ModLoader.AddName(obj, s);
	}	
	
	public static void setField804 (EntityItem item, float value) {
		item.field_804_d = value;
	}
	
	public static File getPropertyFile() {
		return new File(Minecraft.getMinecraftDir(),
				"/config/BuildCraft.cfg");
	}
	
	public static void sendToPlayers(Packet230ModLoader packet, int x, int y,
			int z, int maxDistance, BaseModMp mod) {

	}
	
	public static boolean isPlainBlock (Block block) {
		return block.renderAsNormalBlock();
	}

}
