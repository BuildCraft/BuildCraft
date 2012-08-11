/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.core;

import java.io.File;
import java.util.Arrays;
import java.util.Random;

import buildcraft.core.ItemBlockBuildCraft;
import buildcraft.core.network.BuildCraftPacket;

import net.minecraft.client.Minecraft;
import net.minecraft.src.Block;
import net.minecraft.src.Entity;
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
import net.minecraft.src.WorldClient;
import net.minecraftforge.client.MinecraftForgeClient;

import cpw.mods.fml.client.SpriteHelper;
import cpw.mods.fml.common.FMLCommonHandler;

public class CoreProxy {

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
	
	public static void addCraftingRecipe(ItemStack result, Object[] recipe) {
		ModLoader.addRecipe(result, recipe);
	}
	
	public static void addShapelessRecipe(ItemStack result, Object[] recipe) {
		ModLoader.addShapelessRecipe(result, recipe);
	}

	public static void onCraftingPickup(World world, EntityPlayer player, ItemStack stack) {
		stack.onCrafting(world, player, stack.stackSize);
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

	public static void TakenFromCrafting(EntityPlayer entityplayer, ItemStack itemstack, IInventory iinventory) {
		ModLoader.takenFromCrafting(entityplayer, itemstack, iinventory);
	}

	public static String playerName() {
		return ModLoader.getMinecraftInstance().thePlayer.username;
	}

	/* FORMER API PROXY */
	public static boolean isClient(World world) {
		return world instanceof WorldClient;
	}

	public static boolean isServerSide() {
		return false;
	}

	public static boolean isRemote() {
		return ModLoader.getMinecraftInstance().theWorld.isRemote;
	}

	public static void removeEntity(Entity entity) {
		entity.setDead();
	
		if (entity.worldObj instanceof WorldClient)
			((WorldClient) entity.worldObj).removeEntityFromWorld(entity.entityId);
	}

	public static Random createNewRandom(World world) {
		return new Random(world.getSeed());
	}

	/* BUILDCRAFT PLAYER */
	private static EntityPlayer buildCraftPlayer;
	
	private static EntityPlayer createNewPlayer(World world) {
		return new EntityPlayer(world) {
			@Override
			public void func_6420_o() {}
		};
	}

	public static EntityPlayer getBuildCraftPlayer(World world) {
		if (CoreProxy.buildCraftPlayer == null) {
			CoreProxy.buildCraftPlayer = createNewPlayer(world);
		}
	
		return CoreProxy.buildCraftPlayer;
	}
	
	public static String getItemDisplayName(ItemStack stack){
		if (Item.itemsList[stack.itemID] == null) return "";
		
		return Item.itemsList[stack.itemID].getItemDisplayName(stack);
	}
}
