/** 
 * BuildCraft is open-source. It is distributed under the terms of the 
 * BuildCraft Open Source License. It grants rights to read, modify, compile
 * or run the code. It does *NOT* grant the right to redistribute this software
 * or its modifications in any form, binary or source, except if expressively
 * granted by the copyright holder.
 */

package net.minecraft.src.buildcraft.core;

import java.io.File;
import java.util.HashMap;

import net.minecraft.client.Minecraft;
import net.minecraft.src.BaseModMp;
import net.minecraft.src.Block;
import net.minecraft.src.BuildCraftCore;
import net.minecraft.src.EntityItem;
import net.minecraft.src.ModLoader;
import net.minecraft.src.ModTextureStatic;
import net.minecraft.src.Packet230ModLoader;
import net.minecraft.src.RenderEngine;

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

	public static int addFuel (int id, int dmg) {
		return ModLoader.AddAllFuel(id, dmg);
	}
	
		
	// Change this one to the first empty row, at the moment it is row 8
	private static int textureStartingIndex = 9 * 16;
	// The current free index
	private static int textureIndex = textureStartingIndex;
	// This is calculated by the amount of open slots until there is another
	// block on the sheet. Starting at row 8 you have 3 empty rows of 16 + 13
	// empty in the next row
	private static int textureStopIndex = textureStartingIndex + 61;
	// This is the current MC texture index for BC
	private static int coreTextureIndex = -1;
		
	/**
	 * Adds an override to the buildcraft texture file, mainly to provide 
	 * pipes with icons.
	 */
	public static int addCustomTexture(String pathToTexture) {
		loadTextureIndex();
		try {
			if (textureIndex >= textureStopIndex) {
				System.out.println("Out of BuildCraft Textures!");
				return 0;
			}
			ModTextureStatic modtexturestatic;
			modtexturestatic = new ModTextureStatic(textureIndex,
					coreTextureIndex, ModLoader.loadImage(
							ModLoader.getMinecraftInstance().renderEngine,
							pathToTexture));
			ModLoader.getMinecraftInstance().renderEngine
					.registerTextureFX(modtexturestatic);
			System.out.println("Overriding " + BuildCraftCore.customBuildCraftTexture + " @ "
					+ textureIndex + " With " + pathToTexture + ". "
					+ (textureStopIndex - textureIndex) + " left.");
			int i = textureIndex;
			textureIndex++;
			return i;
		} catch (Exception e) {
			e.printStackTrace();
			return 0; // Error, but the game won't crash, just have the wrong
						// texture
		}

	}

	@SuppressWarnings("unchecked")
	private static void loadTextureIndex() {
		if (coreTextureIndex >= 0)
			return;
		HashMap <String, Integer> textures = new HashMap <String, Integer>();
		try {
			textures = (HashMap <String, Integer>) ModLoader.getPrivateValue(RenderEngine.class,
					ModLoader.getMinecraftInstance().renderEngine, 1);
		} catch (Exception e) {
			e.printStackTrace();
		}
		coreTextureIndex = (Integer) textures
				.get(BuildCraftCore.customBuildCraftTexture);
	}
}
