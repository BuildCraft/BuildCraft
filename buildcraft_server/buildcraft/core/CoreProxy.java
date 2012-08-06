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
import java.util.Random;

import buildcraft.core.network.BuildCraftPacket;

import net.minecraft.src.Block;
import net.minecraft.src.Entity;
import net.minecraft.src.EntityItem;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.EntityPlayerMP;
import net.minecraft.src.IBlockAccess;
import net.minecraft.src.IInventory;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.ModLoader;
import net.minecraft.src.Packet;
import net.minecraft.src.World;
import net.minecraft.src.forge.NetworkMod;

public class CoreProxy {

	public static String getCurrentLanguage() {
		return null;
	}

	// / FIXING GENERAL MLMP AND DEOBFUSCATION DERPINESS
	public static void addName(Object obj, String s) {}

	public static void registerBlock(Block block) {
		Item.itemsList[block.blockID] = null;
		Item.itemsList[block.blockID] = new ItemBlockBuildCraft(block.blockID - 256, block.getBlockName());
	}

	public static void registerTileEntity(Class clas, String ident) {
		ModLoader.registerTileEntity(clas, ident);
	}

	public static void onCraftingPickup(World world, EntityPlayer player, ItemStack stack) {
		stack.onCrafting(world, player, stack.stackSize);
	}
	
	public static void addCraftingRecipe(ItemStack result, Object[] recipe) {
		ModLoader.addRecipe(result, recipe);
	}
	
	public static void addShapelessRecipe(ItemStack result, Object[] recipe) {
		ModLoader.addShapelessRecipe(result, recipe);
	}

	public static File getPropertyFile() {
		return new File("BuildCraft.cfg");
	}

	public static void sendToPlayers(Packet packet, World w, int x, int y, int z, int maxDistance, NetworkMod mod) {
		if (packet != null) {
			for (int j = 0; j < w.playerEntities.size(); j++) {
				EntityPlayerMP player = (EntityPlayerMP) w.playerEntities.get(j);

				if (Math.abs(player.posX - x) <= maxDistance && Math.abs(player.posY - y) <= maxDistance
						&& Math.abs(player.posZ - z) <= maxDistance)
					player.playerNetServerHandler.sendPacket(packet);
			}
		}
	}

	public static void sendToPlayer(EntityPlayer entityplayer, BuildCraftPacket packet) {
		EntityPlayerMP player = (EntityPlayerMP) entityplayer;
		player.playerNetServerHandler.sendPacket(packet.getPacket());
	}

	public static void sendToServer(Packet packet) {}

	public static File getBuildCraftBase() {
		return new File("buildcraft/");
	}

	public static void addLocalization(String s1, String string) {}

	public static int addCustomTexture(String pathToTexture) {
		return 0;
	}

	public static void TakenFromCrafting(EntityPlayer thePlayer, ItemStack itemstack, IInventory craftMatrix) {
		ModLoader.takenFromCrafting(thePlayer, itemstack, craftMatrix);
	}

	/* FORMER API PROXY */
	public static boolean isClient(World world) {
		return false;
	}

	public static boolean isServerSide() {
		return true;
	}

	public static boolean isRemote() {
		return false;
	}

	public static void removeEntity(Entity entity) {
		entity.setDead();
	}

	public static Random createNewRandom(World world) {
		return new Random(world.getSeed());
	}

	/* BUILDCRAFT PLAYER */
	private static EntityPlayer buildCraftPlayer;
	
	private static EntityPlayer createNewPlayer(World world) {
		return new EntityPlayer(world) {

		};
	}

	public static EntityPlayer getBuildCraftPlayer(World world) {
		if (CoreProxy.buildCraftPlayer == null) {
			CoreProxy.buildCraftPlayer = createNewPlayer(world);
		}
	
		return CoreProxy.buildCraftPlayer;
	}

	public static String getItemDisplayName(ItemStack newStack) {
		return "";
	}

}
