/**
 * Copyright (c) SpaceToad, 2011 http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License
 * 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.proxy;

import buildcraft.api.core.LaserKind;
import buildcraft.core.EntityBlock;
import buildcraft.core.ItemBlockBuildCraft;
import buildcraft.core.network.BuildCraftPacket;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.registry.GameRegistry;
import java.io.File;
import java.util.List;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.network.packet.Packet;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatMessageComponent;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

public class CoreProxy {

	@SidedProxy(clientSide = "buildcraft.core.proxy.CoreProxyClient", serverSide = "buildcraft.core.proxy.CoreProxy")
	public static CoreProxy proxy;

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
		entity.worldObj.removeEntity(entity);
	}

	/* WRAPPER */
	@SuppressWarnings("rawtypes")
	public void feedSubBlocks(int id, CreativeTabs tab, List itemList) {
	}

	/* LOCALIZATION */
	public void addName(Object obj, String s) {
	}

	public void addLocalization(String s1, String string) {
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
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(result, recipe));
		//GameRegistry.addRecipe(result, recipe);
	}

	public void addShapelessRecipe(ItemStack result, Object... recipe) {
	        CraftingManager.getInstance().getRecipeList().add(new ShapelessOreRecipe(result, recipe));
	        //GameRegistry.addShapelessRecipe(result, recipe);
	}

	public void sendToPlayers(Packet packet, World world, int x, int y, int z, int maxDistance) {
		if (packet != null) {
			for (int j = 0; j < world.playerEntities.size(); j++) {
				EntityPlayerMP player = (EntityPlayerMP) world.playerEntities.get(j);

				if (Math.abs(player.posX - x) <= maxDistance && Math.abs(player.posY - y) <= maxDistance && Math.abs(player.posZ - z) <= maxDistance) {
					player.playerNetServerHandler.sendPacketToPlayer(packet);
				}
			}
		}
	}

	public void sendToPlayer(EntityPlayer entityplayer, BuildCraftPacket packet) {
		EntityPlayerMP player = (EntityPlayerMP) entityplayer;
		player.playerNetServerHandler.sendPacketToPlayer(packet.getPacket());
	}

	public void sendToServer(Packet packet) {
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

	public String playerName() {
		return "";
	}

	private EntityPlayer createNewPlayer(World world) {
		EntityPlayer player = new EntityPlayer(world, "[BuildCraft]") {
			@Override
			public void sendChatToPlayer(ChatMessageComponent var1) {
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
		EntityPlayer player = new EntityPlayer(world, "[BuildCraft]") {
			@Override
			public void sendChatToPlayer(ChatMessageComponent var1) {
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
}
