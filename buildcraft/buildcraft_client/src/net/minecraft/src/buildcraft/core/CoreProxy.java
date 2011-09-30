/** 
 * BuildCraft is open-source. It is distributed under the terms of the 
 * BuildCraft Open Source License. It grants rights to read, modify, compile
 * or run the code. It does *NOT* grant the right to redistribute this software
 * or its modifications in any form, binary or source, except if expressively
 * granted by the copyright holder.
 */

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
	
	public static File getBuildCraftBase () {
		return new File(Minecraft.getMinecraftDir(), "/buildcraft/");
	}
	
	public static void sendToPlayers(Packet230ModLoader packet, int x, int y,
			int z, int maxDistance, BaseModMp mod) {

	}
	
	public static boolean isPlainBlock (Block block) {
		return block.renderAsNormalBlock();
	}

	public static void addLocalization(String s1, String string) {
		ModLoader.AddLocalization(s1, string);
		
	}

}
