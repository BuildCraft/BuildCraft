/**
 * Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.proxy;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.INetHandler;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;

import buildcraft.core.BuildCraftCore;
import buildcraft.core.lib.EntityBlock;
import buildcraft.core.lib.engines.RenderEngine;
import buildcraft.core.lib.engines.TileEngineBase;
import buildcraft.core.lib.render.RenderEntityBlock;
import buildcraft.core.render.RenderingEntityBlocks;

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

	@Override
	public void initializeRendering() {
		//TODO Update me to grab differing trunk textures
		ClientRegistry.bindTileEntitySpecialRenderer(TileEngineBase.class, new RenderEngine());
		for (int i = 0; i < BuildCraftCore.engineBlock.getEngineCount(); i++) {
			RenderingEntityBlocks.blockByEntityRenders.put(new RenderingEntityBlocks.EntityRenderIndex(BuildCraftCore.engineBlock, i), new RenderEngine((TileEngineBase) BuildCraftCore.engineBlock.createTileEntity(null, i)));
		}
	}

	@Override
	public void initializeEntityRendering() {
		RenderingRegistry.registerEntityRenderingHandler(EntityBlock.class, RenderEntityBlock.INSTANCE);
	}

	/* BUILDCRAFT PLAYER */
	@Override
	public String playerName() {
		return FMLClientHandler.instance().getClient().thePlayer.getDisplayNameString();
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
}
