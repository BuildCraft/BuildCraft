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

import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.network.PacketDispatcher;

import buildcraft.core.network.BuildCraftPacket;

import net.minecraft.src.Block;
import net.minecraft.src.Entity;
import net.minecraft.src.EntityItem;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.EntityPlayerMP;
import net.minecraft.src.IInventory;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.ModLoader;
import net.minecraft.src.Packet;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;

public class ProxyCore {

	@SidedProxy(clientSide="buildcraft.core.ClientProxyCore", serverSide="buildcraft.core.ProxyCore")
	public static ProxyCore proxy;
	
	/* SIMULATION */
	public boolean isSimulating(World world) {
		return !world.isRemote;
	}

	public boolean isRemote(World world) {
		return world.isRemote;
	}

	public String getCurrentLanguage() {
		return null;
	}

	/* ENTITY HANDLING */
	public void removeEntity(Entity entity) {
		entity.setDead();
	}

	/* LOCALIZATION */
	public void addName(Object obj, String s) {}
	public void addLocalization(String s1, String string) {}
	public String getItemDisplayName(ItemStack newStack) { return ""; }

	/* GFX */
	public void obsidianPipePickup(World world, EntityItem item, TileEntity tile) {}
	
	/* REGISTRATION */
	public void registerBlock(Block block) {
		Item.itemsList[block.blockID] = null;
		Item.itemsList[block.blockID] = new ItemBlockBuildCraft(block.blockID - 256, block.getBlockName());
	}

	public void registerTileEntity(Class clas, String ident) {
		ModLoader.registerTileEntity(clas, ident);
	}

	public void onCraftingPickup(World world, EntityPlayer player, ItemStack stack) {
		stack.onCrafting(world, player, stack.stackSize);
	}
	
	public void addCraftingRecipe(ItemStack result, Object[] recipe) {
		ModLoader.addRecipe(result, recipe);
	}
	
	public void addShapelessRecipe(ItemStack result, Object[] recipe) {
		ModLoader.addShapelessRecipe(result, recipe);
	}

	public static void sendToPlayers(Packet packet, World w, int x, int y, int z, int maxDistance, String channel) {
		if(packet!=null)
		PacketDispatcher.sendPacketToAllAround(x, y, z, maxDistance, w.getWorldInfo().getDimension(), packet);
	}

	public static void sendToPlayer(EntityPlayer entityplayer, BuildCraftPacket packet) {
		if(packet!=null)
		PacketDispatcher.sendPacketToPlayer(packet.getPacket(), (Player)entityplayer);
	}

	public static void sendToServer(Packet packet) {
		PacketDispatcher.sendPacketToServer(packet);
	}


	/* FILE SYSTEM */
	public File getBuildCraftBase() {
		return new File("./");
	}

	public int addCustomTexture(String pathToTexture) {
		return 0;
	}

	public void TakenFromCrafting(EntityPlayer thePlayer, ItemStack itemstack, IInventory craftMatrix) {
		ModLoader.takenFromCrafting(thePlayer, itemstack, craftMatrix);
	}

	public Random createNewRandom(World world) {
		return new Random(world.getSeed());
	}

	/* BUILDCRAFT PLAYER */
	protected static EntityPlayer buildCraftPlayer;
	
	public String playerName() { return ""; }
	private EntityPlayer createNewPlayer(World world) {
		return new EntityPlayer(world) {

			@Override
			public void sendChatToPlayer(String var1) {
			}

			@Override
			public boolean canCommandSenderUseCommand(String var1) {
				return false;
			}

		};
	}

	public EntityPlayer getBuildCraftPlayer(World world) {
		if (ProxyCore.buildCraftPlayer == null) {
			ProxyCore.buildCraftPlayer = createNewPlayer(world);
		}
	
		return ProxyCore.buildCraftPlayer;
	}

}
