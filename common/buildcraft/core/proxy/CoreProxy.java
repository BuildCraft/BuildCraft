/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.proxy;

import java.util.List;
import java.util.Random;

import com.mojang.authlib.GameProfile;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.network.INetHandler;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.World;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.registry.GameRegistry;

import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import buildcraft.BuildCraftCore;
import buildcraft.api.core.ICoreProxy;
import buildcraft.core.EntityBlock;
import buildcraft.core.ItemBlockBuildCraft;
import buildcraft.core.LaserKind;

public class CoreProxy implements ICoreProxy {

	@SidedProxy(clientSide = "buildcraft.core.proxy.CoreProxyClient", serverSide = "buildcraft.core.proxy.CoreProxy")
	public static CoreProxy proxy;

	/* BUILDCRAFT PLAYER */
	protected static EntityPlayer buildCraftPlayer;

	public String getMinecraftVersion() {
		return Loader.instance().getMinecraftModContainer().getVersion();
	}

	/* INSTANCES */
	public Object getClient() {
		return null;
	}

	public World getClientWorld() {
		return null;
	}

	/* ENTITY HANDLING */
	public void removeEntity(Entity entity) {
		entity.worldObj.removeEntity(entity);
	}

	/* WRAPPER */
	@SuppressWarnings("rawtypes")
	public void feedSubBlocks(Block block, CreativeTabs tab, List itemList) {
	}

	public String getItemDisplayName(ItemStack newStack) {
		return "";
	}

	/* GFX */
	public void obsidianPipePickup(World world, EntityItem item, TileEntity tile) {
	}

	public void initializeRendering() {
	}

	public void initializeEntityRendering() {
	}

	/* REGISTRATION */
	public void registerBlock(Block block) {
		registerBlock(block, ItemBlockBuildCraft.class);
	}

	public void registerBlock(Block block, Class<? extends ItemBlock> item) {
		GameRegistry.registerBlock(block, item, block.getUnlocalizedName().replace("tile.", ""));
	}

	public void registerItem(Item item) {
		GameRegistry.registerItem(item, item.getUnlocalizedName().replace("item.", ""));
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	public void registerTileEntity(Class clas, String ident) {
		GameRegistry.registerTileEntity(clas, ident);
	}

	public void onCraftingPickup(World world, EntityPlayer player, ItemStack stack) {
		stack.onCrafting(world, player, stack.stackSize);
	}

	@SuppressWarnings("unchecked")
	public void addCraftingRecipe(ItemStack result, Object... recipe) {
		String name = Item.itemRegistry.getNameForObject(result.getItem());

		if (BuildCraftCore.recipesBlacklist.contains(name)) {
			return;
		}

		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(result, recipe));
	}

	public void addShapelessRecipe(ItemStack result, Object... recipe) {
		String name = Item.itemRegistry.getNameForObject(result.getItem());

		if (BuildCraftCore.recipesBlacklist.contains(name)) {
			return;
		}

		CraftingManager.getInstance().getRecipeList().add(new ShapelessOreRecipe(result, recipe));
	}

	public int addCustomTexture(String pathToTexture) {
		return 0;
	}

	public Random createNewRandom(World world) {
		return new Random(world.getSeed());
	}

	public String playerName() {
		return "";
	}

	private EntityPlayer createNewPlayer(World world) {
		EntityPlayer player = new EntityPlayer(world, new GameProfile (null, "[BuildCraft]")) {
			@Override
			public void addChatMessage(IChatComponent var1) {
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
		return player;
	}

	private EntityPlayer createNewPlayer(World world, int x, int y, int z) {
		EntityPlayer player = new EntityPlayer(world, new GameProfile (null, "[BuildCraft]")) {
			@Override
			public void addChatMessage(IChatComponent var1) {
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
		player.posX = x;
		player.posY = y;
		player.posZ = z;
		return player;
	}

	@Override
	public EntityPlayer getBuildCraftPlayer(World world) {
		if (CoreProxy.buildCraftPlayer == null) {
			CoreProxy.buildCraftPlayer = createNewPlayer(world);
		} else {
			CoreProxy.buildCraftPlayer.worldObj = world;
		}

		return CoreProxy.buildCraftPlayer;
	}

	public EntityPlayer getBuildCraftPlayer(World world, int x, int y, int z) {
		if (CoreProxy.buildCraftPlayer == null) {
			CoreProxy.buildCraftPlayer = createNewPlayer(world, x, y, z);
		} else {
			CoreProxy.buildCraftPlayer.worldObj = world;
			CoreProxy.buildCraftPlayer.posX = x;
			CoreProxy.buildCraftPlayer.posY = y;
			CoreProxy.buildCraftPlayer.posZ = z;
		}

		return CoreProxy.buildCraftPlayer;
	}

	public EntityBlock newEntityBlock(World world, double i, double j, double k, double iSize, double jSize, double kSize, LaserKind laserKind) {
		return new EntityBlock(world, i, j, k, iSize, jSize, kSize);
	}

	/**
	 * This function returns either the player from the handler if it's on the
	 * server, or directly from the minecraft instance if it's the client.
	 */
	public EntityPlayer getPlayerFromNetHandler (INetHandler handler) {
		if (handler instanceof NetHandlerPlayServer) {
			return ((NetHandlerPlayServer) handler).playerEntity;
		} else {
			return null;
		}
	}
}
