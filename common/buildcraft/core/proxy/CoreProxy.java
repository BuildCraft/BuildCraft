/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.core.proxy;

import java.io.File;
import java.util.List;
import java.util.Random;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.registry.GameRegistry;

import buildcraft.core.ItemBlockBuildCraft;
import buildcraft.core.network.BuildCraftPacket;

import net.minecraft.src.Block;
import net.minecraft.src.ChunkCoordinates;
import net.minecraft.src.CreativeTabs;
import net.minecraft.src.Entity;
import net.minecraft.src.EntityItem;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.EntityPlayerMP;
import net.minecraft.src.IInventory;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.Packet;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;

public class CoreProxy {

	@SidedProxy(clientSide="buildcraft.core.proxy.CoreProxyClient", serverSide="buildcraft.core.proxy.CoreProxy")
	public static CoreProxy proxy;

	public String getMinecraftVersion() { return Loader.instance().getMinecraftModContainer().getVersion(); }

	/* INSTANCES */
	public Object getClient() { return null; }
	public World getClientWorld() { return null; }

	/* SIMULATION */
	public boolean isSimulating(World world) {
		return !world.isRemote;
	}

	public boolean isRenderWorld(World world) {
		return world.isRemote;
	}

	public String getCurrentLanguage() {
		return null;
	}

	/* ENTITY HANDLING */
	public void removeEntity(Entity entity) {
		entity.setDead();
	}

	/* WRAPPER */
	public void feedSubBlocks(int id, CreativeTabs tab, List itemList) {}

	/* LOCALIZATION */
	public void addName(Object obj, String s) {}
	public void addLocalization(String s1, String string) {}
	public String getItemDisplayName(ItemStack newStack) { return ""; }

	/* GFX */
	public void obsidianPipePickup(World world, EntityItem item, TileEntity tile) {}
	public void initializeRendering() {}
	public void initializeEntityRendering() {}

	/* REGISTRATION */
	public void registerBlock(Block block) {
		Item.itemsList[block.blockID] = null;
		Item.itemsList[block.blockID] = new ItemBlockBuildCraft(block.blockID - 256, block.getBlockName());
	}

	public void registerTileEntity(Class clas, String ident) {
		GameRegistry.registerTileEntity(clas, ident);
	}

	public void onCraftingPickup(World world, EntityPlayer player, ItemStack stack) {
		stack.onCrafting(world, player, stack.stackSize);
	}

	public void addCraftingRecipe(ItemStack result, Object[] recipe) {
		GameRegistry.addRecipe(result, recipe);
	}

	public void addShapelessRecipe(ItemStack result, Object[] recipe) {
		GameRegistry.addShapelessRecipe(result, recipe);
	}

	public void sendToPlayers(Packet packet, World world, int x, int y, int z, int maxDistance) {
		if (packet != null) {
			for (int j = 0; j < world.playerEntities.size(); j++) {
				EntityPlayerMP player = (EntityPlayerMP) world.playerEntities.get(j);

				if (Math.abs(player.posX - x) <= maxDistance && Math.abs(player.posY - y) <= maxDistance
						&& Math.abs(player.posZ - z) <= maxDistance)
					player.playerNetServerHandler.sendPacketToPlayer(packet);
			}
		}
	}

	public void sendToPlayer(EntityPlayer entityplayer, BuildCraftPacket packet) {
		EntityPlayerMP player = (EntityPlayerMP) entityplayer;
		player.playerNetServerHandler.sendPacketToPlayer(packet.getPacket());
	}

	public void sendToServer(Packet packet) {}

	/* FILE SYSTEM */
	public File getBuildCraftBase() {
		return new File("./");
	}

	public int addCustomTexture(String pathToTexture) {
		return 0;
	}

	public void TakenFromCrafting(EntityPlayer thePlayer, ItemStack itemstack, IInventory craftMatrix) {
		GameRegistry.onItemCrafted(thePlayer, itemstack, craftMatrix);
	}

	public Random createNewRandom(World world) {
		return new Random(world.getSeed());
	}

	/* BUILDCRAFT PLAYER */
	protected static EntityPlayer buildCraftPlayer;

	public String playerName() { return ""; }
	private EntityPlayer createNewPlayer(World world) {
		EntityPlayer player = new EntityPlayer(world) {

			@Override
			public void sendChatToPlayer(String var1) {
			}

			@Override
			public boolean canCommandSenderUseCommand(int var1, String var2) {
				return false;
			}

			@Override
			public ChunkCoordinates getPlayerCoordinates() {
				return null;
			}

		};
		player.username = "[BuildCraft]";
		return player;
	}

	public EntityPlayer getBuildCraftPlayer(World world) {
		if (CoreProxy.buildCraftPlayer == null) {
			CoreProxy.buildCraftPlayer = createNewPlayer(world);
		}

		return CoreProxy.buildCraftPlayer;
	}

}
