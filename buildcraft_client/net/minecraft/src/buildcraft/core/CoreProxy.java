/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src.buildcraft.core;

import java.io.File;
import java.util.Arrays;

import net.minecraft.client.Minecraft;
import net.minecraft.src.Block;
import net.minecraft.src.EntityItem;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.IBlockAccess;
import net.minecraft.src.IInventory;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.ModLoader;
import net.minecraft.src.Packet;
import net.minecraft.src.StringTranslate;
import net.minecraft.src.World;
import net.minecraft.src.buildcraft.core.network.BuildCraftPacket;
import net.minecraft.src.forge.MinecraftForgeClient;
import net.minecraft.src.forge.NetworkMod;
import cpw.mods.fml.client.SpriteHelper;

public class CoreProxy {

	private static boolean registeredOverrideTextureMap = false;

	public static String getCurrentLanguage() {
		return StringTranslate.getInstance().getCurrentLanguage();
	}

	// / FIXING GENERAL MLMP AND DEOBFUSCATION DERPINESS
	public static void addName(Object obj, String s) {
		ModLoader.addName(obj, s);
	}

	public static void registerBlock(Block block) {
		Item.itemsList[block.blockID] = null;
		Item.itemsList[block.blockID] = new ItemBlockBuildCraft(block.blockID - 256, block.getBlockName());
	}

	public static void registerTileEntity(@SuppressWarnings("rawtypes") Class clas, String ident) {
		ModLoader.registerTileEntity(clas, ident);
	}

	/*
	 * public static void registerGUI(BaseModMp basemod, int id) {
	 * ModLoaderMp.registerGUI(basemod, id); }
	 */
	public static void onCraftingPickup(World world, EntityPlayer player, ItemStack stack) {
		stack.onCrafting(world, player, stack.stackSize);
	}

	public static void setField804(EntityItem item, float value) {
		item.field_804_d = value;
	}

	public static File getPropertyFile() {
		return new File(Minecraft.getMinecraftDir(), "/config/BuildCraft.cfg");
	}

	public static File getBuildCraftBase() {
		return new File(Minecraft.getMinecraftDir(), "/buildcraft/");
	}

	public static void sendToPlayers(Packet packet, World w, int x, int y, int z, int maxDistance, NetworkMod mod) {}

	public static void sendToPlayer(EntityPlayer entityplayer, BuildCraftPacket packet) {}

	public static void sendToServer(Packet packet) {
		// ModLoaderMp.sendPacket(mod, packet);
		ModLoader.getMinecraftInstance().getSendQueue().addToSendQueue(packet);
	}

	public static void addLocalization(String s1, String string) {
		ModLoader.addLocalization(s1, string);

	}

	public static int addFuel(int id, int dmg) {
		return ModLoader.addAllFuel(id, dmg);
	}

	/**
	 * Adds an override to the buildcraft texture file, mainly to provide pipes
	 * with icons.
	 */
	public static int addCustomTexture(String pathToTexture) {
		if (!registeredOverrideTextureMap) {
			char[] map = new char[256];
			// every slot is free!
			Arrays.fill(map, '1');
			SpriteHelper.registerSpriteMapForFile(DefaultProps.TEXTURE_EXTERNAL, new String(map));
			registeredOverrideTextureMap = true;
		}
		return ModLoader.addOverride(DefaultProps.TEXTURE_EXTERNAL, pathToTexture) + 256;
	}

	public static long getHash(IBlockAccess iBlockAccess) {
		// return iBlockAccess.func_48454_a(x, z).hashCode();
		return 0;
	}

	public static void TakenFromCrafting(EntityPlayer entityplayer, ItemStack itemstack, IInventory iinventory) {
		ModLoader.takenFromCrafting(entityplayer, itemstack, iinventory);
	}

	public static String playerName() {
		return ModLoader.getMinecraftInstance().thePlayer.username;
	}

	public static void BindTexture(String texture) {
		MinecraftForgeClient.bindTexture(texture);

	}
}
