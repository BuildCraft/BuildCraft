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

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;

import buildcraft.BuildCraftCore;
import buildcraft.core.render.RenderEnergyLaser;
import buildcraft.core.render.RenderEntityBlock;
import buildcraft.core.render.RenderLaser;
import buildcraft.core.render.RenderRobot;
import buildcraft.core.render.RenderingEntityBlocks;
import buildcraft.core.render.RenderingMarkers;
import buildcraft.core.render.RenderingOil;
import buildcraft.transport.render.TileEntityPickupFX;

import net.minecraft.client.Minecraft;
import net.minecraft.src.Entity;
import net.minecraft.src.EntityItem;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.Packet;
import net.minecraft.src.StringTranslate;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import net.minecraft.src.WorldClient;
import net.minecraftforge.client.MinecraftForgeClient;

public class ClientProxyCore extends ProxyCore {

	/* ENTITY HANDLING */
	@Override
	public void removeEntity(Entity entity) {
		super.removeEntity(entity);

		if (isRemote(entity.worldObj))
			((WorldClient) entity.worldObj).removeEntityFromWorld(entity.entityId);
	}

	/* LOCALIZATION */
	@Override
	public String getCurrentLanguage() {
		return StringTranslate.getInstance().getCurrentLanguage();
	}
	@Override
	public void addName(Object obj, String s) {
		LanguageRegistry.addName(obj, s);
	}
	@Override
	public void addLocalization(String s1, String string) {
		LanguageRegistry.instance().addStringLocalization(s1, string);
	}
	@Override
	public String getItemDisplayName(ItemStack stack){
		if (Item.itemsList[stack.itemID] == null) return "";

		return Item.itemsList[stack.itemID].getItemDisplayName(stack);
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
		BuildCraftCore.oilModel = RenderingRegistry.getNextAvailableRenderId();

		RenderingRegistry.registerBlockHandler(new RenderingEntityBlocks());
		RenderingRegistry.registerBlockHandler(BuildCraftCore.legacyPipeModel, new RenderingEntityBlocks());
		RenderingRegistry.registerBlockHandler(new RenderingOil());
		RenderingRegistry.registerBlockHandler(new RenderingMarkers());

		MinecraftForgeClient.preloadTexture(DefaultProps.TEXTURE_BLOCKS);
		MinecraftForgeClient.preloadTexture(DefaultProps.TEXTURE_ITEMS);
	}

	@Override
	public void initializeEntityRendering() {
		RenderingRegistry.registerEntityRenderingHandler(EntityBlock.class, new RenderEntityBlock());
		RenderingRegistry.registerEntityRenderingHandler(EntityLaser.class, new RenderLaser());
		RenderingRegistry.registerEntityRenderingHandler(EntityEnergyLaser.class, new RenderEnergyLaser());
		RenderingRegistry.registerEntityRenderingHandler(EntityRobot.class, new RenderRobot());
	}


	/* NETWORKING */
	@Override
	public void sendToServer(Packet packet) {
		FMLClientHandler.instance().getClient().getSendQueue().addToSendQueue(packet);
	}

	/* FILE SYSTEM */
	public File getBuildCraftBase() {
		return Minecraft.getMinecraftDir();
	}

	/* BUILDCRAFT PLAYER */
	@Override
	public String playerName() {
		return FMLClientHandler.instance().getClient().thePlayer.username;
	}

	private EntityPlayer createNewPlayer(World world) {
		return new EntityPlayer(world) {
			@Override public void sendChatToPlayer(String var1) {}
			@Override public boolean canCommandSenderUseCommand(String var1) { return false; }
		};
	}

	@Override
	public EntityPlayer getBuildCraftPlayer(World world) {
		if (ProxyCore.buildCraftPlayer == null) {
			ProxyCore.buildCraftPlayer = createNewPlayer(world);
		}

		return ProxyCore.buildCraftPlayer;
	}

}
