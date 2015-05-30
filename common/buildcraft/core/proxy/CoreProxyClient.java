/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.proxy;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.INetHandler;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.IStringSerializable;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.client.MinecraftForgeClient;
import buildcraft.BuildCraftCore;
import buildcraft.BuildCraftSilicon;
import buildcraft.api.enums.EnumColor;
import buildcraft.core.EntityBlock;
import buildcraft.core.ItemBuildCraft;
import buildcraft.core.LaserKind;
import buildcraft.core.render.RenderEntityBlock;
import buildcraft.core.render.RenderRobot;
import buildcraft.core.render.RenderingEntityBlocks;
import buildcraft.core.render.RenderingMarkers;
import buildcraft.core.robots.EntityRobot;
import buildcraft.core.utils.IModelRegister;
import buildcraft.core.utils.Utils;
import buildcraft.transport.render.TileEntityPickupFX;

public class CoreProxyClient extends CoreProxy {

	/* INSTANCES */
	@Override
	public Object getClient() {
		return FMLClientHandler.instance().getClient();
	}

	@Override
	public World getClientWorld() {
		return FMLClientHandler.instance().getClient().theWorld;
	}

	/* ENTITY HANDLING */
	@Override
	public void removeEntity(Entity entity) {
		super.removeEntity(entity);

		if (entity.worldObj.isRemote) {
			((WorldClient) entity.worldObj).removeEntityFromWorld(entity.getEntityId());
		}
	}

	/* WRAPPER */
	@SuppressWarnings("rawtypes")
	@Override
	public void feedSubBlocks(Block block, CreativeTabs tab, List itemList) {
		if (block == null) {
			return;
		}

		block.getSubBlocks(Item.getItemFromBlock(block), tab, itemList);
	}

	@Override
	public String getItemDisplayName(ItemStack stack) {
		if (stack.getItem() == null) {
			return "";
		}

		return stack.getDisplayName();
	}

	/* GFX */
	@Override
	public void obsidianPipePickup(World world, EntityItem item, TileEntity tile) {
		FMLClientHandler.instance().getClient().effectRenderer.addEffect(new TileEntityPickupFX(world, item, tile));
	}

	@Override
	public void initializeRendering() {
		/*BuildCraftCore.blockByEntityModel = RenderingRegistry.getNextAvailableRenderId();
		BuildCraftCore.legacyPipeModel = RenderingRegistry.getNextAvailableRenderId();
		BuildCraftCore.markerModel = RenderingRegistry.getNextAvailableRenderId();*/

//		RenderingRegistry.registerBlockHandler(new RenderingEntityBlocks());
//		RenderingRegistry.registerBlockHandler(BuildCraftCore.legacyPipeModel, new RenderingEntityBlocks());
//		RenderingRegistry.registerBlockHandler(new RenderingMarkers());
//
//		// TODO: Move these to a Silicon proxy renderer
//		MinecraftForgeClient.registerItemRenderer(BuildCraftSilicon.robotItem, new RenderRobot());
		EnumColor.registerIcons();

		for (Block block : blocksToRegisterRenderersFor) {
			if (block instanceof IModelRegister) {
				((IModelRegister) block).registerModels();
				continue;
			}

			for (IBlockState state : (List<IBlockState>) block.getBlockState().getValidStates()) {
				String type = "";
				for (IProperty property : (Collection<IProperty>) state.getProperties().keySet()) {
				    if (type.length() != 0)
				        type += ",";
					type += property.getName() + "=";
					Object value = state.getValue(property);
					if (value instanceof Integer) {
						type += ((Integer) value).intValue();
					} else if (value instanceof Boolean) {
					    type += ((Boolean) value).toString();
					} else {
						type += ((IStringSerializable) value).getName();
					}
				}
				Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(Item.getItemFromBlock(block), block.damageDropped(state), new ModelResourceLocation(Utils.getBlockName(block), type.toLowerCase()));
				ModelBakery.addVariantName(Item.getItemFromBlock(block), type.toLowerCase());
			}
		}
		for (Item item : itemsToRegisterRenderersFor) {
			if (item instanceof IModelRegister) {
				((IModelRegister) item).registerModels();
			}
		}
	}

	@Override
	public void initializeEntityRendering() {
		RenderingRegistry.registerEntityRenderingHandler(EntityBlock.class, RenderEntityBlock.INSTANCE);
		RenderingRegistry.registerEntityRenderingHandler(EntityRobot.class, new RenderRobot());
	}

	/* BUILDCRAFT PLAYER */
	@Override
	public String playerName() {
		return FMLClientHandler.instance().getClient().thePlayer.getDisplayName().getFormattedText();
	}

	@Override
	public EntityBlock newEntityBlock(World world, double i, double j,	double k, double iSize, double jSize, double kSize, LaserKind laserKind) {
		EntityBlock eb = super.newEntityBlock(world, i, j, k, iSize, jSize, kSize, laserKind);
		switch (laserKind) {
		case Blue:
			//eb.texture = BuildCraftCore.blueLaserTexture;
			break;

		case Red:
			//eb.texture = BuildCraftCore.redLaserTexture;
			break;

		case Stripes:
			//eb.texture = BuildCraftCore.stripesLaserTexture;
			break;
		}
		return eb;
	}

	/**
	 * This function returns either the player from the handler if it's on the
	 * server, or directly from the minecraft instance if it's the client.
	 */
	@Override
	public EntityPlayer getPlayerFromNetHandler (INetHandler handler) {
		if (handler instanceof NetHandlerPlayServer) {
			return ((NetHandlerPlayServer) handler).playerEntity;
		} else {
			return Minecraft.getMinecraft().thePlayer;
		}
	}

	private LinkedList<Block> blocksToRegisterRenderersFor = new LinkedList<Block>();
	private LinkedList<Item> itemsToRegisterRenderersFor = new LinkedList<Item>();

	@Override
	public void registerBlock(Block block, Class<? extends ItemBlock> item) {
		super.registerBlock(block, item);
		blocksToRegisterRenderersFor.add(block);
	}

	@Override
	public void registerItem(Item item) {
		super.registerItem(item);
		itemsToRegisterRenderersFor.add(item);
	}
}
