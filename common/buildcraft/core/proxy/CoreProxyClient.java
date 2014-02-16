/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.proxy;

import buildcraft.BuildCraftCore;
import buildcraft.api.core.LaserKind;
import buildcraft.core.EntityBlock;
import buildcraft.core.EntityEnergyLaser;
import buildcraft.core.EntityPowerLaser;
import buildcraft.core.EntityRobot;
import buildcraft.core.render.RenderEnergyLaser;
import buildcraft.core.render.RenderEntityBlock;
import buildcraft.core.render.RenderLaser;
import buildcraft.core.render.RenderRobot;
import buildcraft.core.render.RenderingEntityBlocks;
import buildcraft.core.render.RenderingMarkers;
import buildcraft.transport.render.TileEntityPickupFX;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;

import java.io.File;
import java.util.List;

import com.mojang.authlib.GameProfile;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.INetHandler;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.Packet;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.World;

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
		if (block == null)
			return;

		block.getSubBlocks(Item.getItemFromBlock(block), tab, itemList);
	}

	@Override
	public String getItemDisplayName(ItemStack stack) {
		if (stack.getItem() == null)
			return "";

		return stack.getDisplayName();
	}

	/* GFX */
	@Override
	public void obsidianPipePickup(World world, EntityItem item, TileEntity tile) {
		FMLClientHandler.instance().getClient().effectRenderer.addEffect(new TileEntityPickupFX(world, item, tile));
	}

	@Override
	public void initializeRendering() {
		BuildCraftCore.blockByEntityModel = RenderingRegistry.getNextAvailableRenderId();
		BuildCraftCore.legacyPipeModel = RenderingRegistry.getNextAvailableRenderId();
		BuildCraftCore.markerModel = RenderingRegistry.getNextAvailableRenderId();

		RenderingRegistry.registerBlockHandler(new RenderingEntityBlocks());
		RenderingRegistry.registerBlockHandler(BuildCraftCore.legacyPipeModel, new RenderingEntityBlocks());
		RenderingRegistry.registerBlockHandler(new RenderingMarkers());
	}

	@Override
	public void initializeEntityRendering() {
		RenderingRegistry.registerEntityRenderingHandler(EntityBlock.class, RenderEntityBlock.INSTANCE);
		RenderingRegistry.registerEntityRenderingHandler(EntityPowerLaser.class, new RenderLaser());
		RenderingRegistry.registerEntityRenderingHandler(EntityEnergyLaser.class, new RenderEnergyLaser());
		RenderingRegistry.registerEntityRenderingHandler(EntityRobot.class, new RenderRobot());
	}

	/* BUILDCRAFT PLAYER */
	@Override
	public String playerName() {
		return FMLClientHandler.instance().getClient().thePlayer.getDisplayName();
	}

	private EntityPlayer createNewPlayer(World world) {
		EntityPlayer player = new EntityPlayer(world, new GameProfile(null, "[BuildCraft]")) {
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

	@Override
	public EntityPlayer getBuildCraftPlayer(World world) {
		if (CoreProxy.buildCraftPlayer == null) {
			CoreProxy.buildCraftPlayer = createNewPlayer(world);
		}

		return CoreProxy.buildCraftPlayer;
	}

	@Override
	public EntityBlock newEntityBlock(World world, double i, double j,	double k, double iSize, double jSize, double kSize, LaserKind laserKind) {
		EntityBlock eb = super.newEntityBlock(world, i, j, k, iSize, jSize, kSize, laserKind);
		switch (laserKind) {
		case Blue:
			eb.texture = BuildCraftCore.blueLaserTexture;
			break;

		case Red:
			eb.texture = BuildCraftCore.redLaserTexture;
			break;

		case Stripes:
			eb.texture = BuildCraftCore.stripesLaserTexture;
			break;
		}
		return eb;
	}
}
