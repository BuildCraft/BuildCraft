/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.proxy;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.INetHandler;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;
import net.minecraftforge.common.DimensionManager;

import buildcraft.BuildCraftCore;
import buildcraft.core.LaserKind;
import buildcraft.core.RenderPathMarker;
import buildcraft.core.TilePathMarker;
import buildcraft.core.lib.EntityBlock;
import buildcraft.core.lib.engines.RenderEngine;
import buildcraft.core.lib.engines.TileEngineBase;
import buildcraft.core.lib.render.RenderBlockComplex;
import buildcraft.core.lib.render.RenderEntityBlock;
import buildcraft.core.render.RenderingEntityBlocks;
import buildcraft.core.render.RenderingMarkers;

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
	@Override
	public String getItemDisplayName(ItemStack stack) {
		if (stack.getItem() == null) {
			return "";
		}

		return stack.getDisplayName();
	}

	@Override
	public void initializeRendering() {
		BuildCraftCore.blockByEntityModel = RenderingRegistry.getNextAvailableRenderId();
		BuildCraftCore.markerModel = RenderingRegistry.getNextAvailableRenderId();
		BuildCraftCore.complexBlockModel = RenderingRegistry.getNextAvailableRenderId();

		RenderingRegistry.registerBlockHandler(new RenderingEntityBlocks());
		RenderingRegistry.registerBlockHandler(new RenderingMarkers());
		RenderingRegistry.registerBlockHandler(BuildCraftCore.complexBlockModel, new RenderBlockComplex());
		ClientRegistry.bindTileEntitySpecialRenderer(TilePathMarker.class, new RenderPathMarker());

		ClientRegistry.bindTileEntitySpecialRenderer(TileEngineBase.class, new RenderEngine());
		for (int i = 0; i < 16; i++) {
			if (BuildCraftCore.engineBlock.hasEngine(i)) {
				TileEngineBase engineTile = (TileEngineBase) BuildCraftCore.engineBlock.createTileEntity(null, i);
				engineTile.blockType = BuildCraftCore.engineBlock;
				engineTile.blockMetadata = i;
				RenderingEntityBlocks.blockByEntityRenders.put(new RenderingEntityBlocks.EntityRenderIndex(BuildCraftCore.engineBlock, i), new RenderEngine(engineTile));
			}
		}
	}

	@Override
	public void initializeEntityRendering() {
		RenderingRegistry.registerEntityRenderingHandler(EntityBlock.class, RenderEntityBlock.INSTANCE);
	}

	/* BUILDCRAFT PLAYER */
	@Override
	public String playerName() {
		return FMLClientHandler.instance().getClient().thePlayer.getDisplayName();
	}

	@Override
	public EntityBlock newEntityBlock(World world, double i, double j, double k, double iSize, double jSize, double kSize, LaserKind laserKind) {
		EntityBlock eb = super.newEntityBlock(world, i, j, k, iSize, jSize, kSize, laserKind);
		switch (laserKind) {
			case Blue:
				eb.setTexture(BuildCraftCore.blueLaserTexture);
				break;

			case Red:
				eb.setTexture(BuildCraftCore.redLaserTexture);
				break;

			case Stripes:
				eb.setTexture(BuildCraftCore.stripesLaserTexture);
				break;
		}
		return eb;
	}

	/**
	 * This function returns either the player from the handler if it's on the
	 * server, or directly from the minecraft instance if it's the client.
	 */
	@Override
	public EntityPlayer getPlayerFromNetHandler(INetHandler handler) {
		if (handler instanceof NetHandlerPlayServer) {
			return ((NetHandlerPlayServer) handler).playerEntity;
		} else {
			return Minecraft.getMinecraft().thePlayer;
		}
	}

	@Override
	public TileEntity getServerTile(TileEntity source) {
		if (BuildCraftCore.useServerDataOnClient && Minecraft.getMinecraft().isSingleplayer() && source.getWorldObj().isRemote) {
			WorldServer w = DimensionManager.getWorld(source.getWorldObj().provider.dimensionId);
			if (w != null && w.getChunkProvider() != null) {
				Chunk c = w.getChunkFromBlockCoords(source.xCoord, source.zCoord);
				if (c != null) {
					TileEntity t = c.getTileEntityUnsafe(source.xCoord & 15, source.yCoord, source.zCoord & 15);
					if (t != null && t.getClass().equals(source.getClass())) {
						return t;
					}
				}
			}
		}
		return source;
	}

	public EntityPlayer getClientPlayer() {
		return Minecraft.getMinecraft().thePlayer;
	}
}
