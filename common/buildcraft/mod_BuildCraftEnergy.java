/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.Init;
import cpw.mods.fml.common.Mod.PostInit;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.registry.GameRegistry;

import buildcraft.BuildCraftEnergy;
import buildcraft.core.DefaultProps;
import buildcraft.core.ProxyCore;
import buildcraft.core.network.PacketHandler;
import buildcraft.core.render.RenderingEntityBlocks;
import buildcraft.core.render.RenderingEntityBlocks.EntityRenderIndex;
import buildcraft.energy.EnergyProxy;
import buildcraft.energy.OilPopulate;
import buildcraft.energy.TileEngine;
import buildcraft.energy.render.RenderEngine;
import buildcraft.energy.render.TextureFuelFX;
import buildcraft.energy.render.TextureOilFX;
import buildcraft.energy.render.TextureOilFlowFX;


@Mod(name="BuildCraft Energy", version=DefaultProps.VERSION, useMetadata = false, modid = "BC|ENERGY")
@NetworkMod(channels = {DefaultProps.NET_CHANNEL_NAME}, packetHandler = PacketHandler.class, clientSideRequired = true, serverSideRequired = true)
public class mod_BuildCraftEnergy {

	public static mod_BuildCraftEnergy instance;

	public mod_BuildCraftEnergy() {
		instance = this;
	}

	@Init
	public void init(FMLInitializationEvent event) {
		BuildCraftEnergy.load();
		GameRegistry.registerWorldGenerator(new OilPopulate());
	}

	@PostInit
	public void postInit(FMLPostInitializationEvent event) {

		BuildCraftEnergy.initialize();

		RenderingEntityBlocks.blockByEntityRenders.put(new EntityRenderIndex(BuildCraftEnergy.engineBlock, 0), new RenderEngine(
				DefaultProps.TEXTURE_PATH_BLOCKS + "/base_wood.png"));
		RenderingEntityBlocks.blockByEntityRenders.put(new EntityRenderIndex(BuildCraftEnergy.engineBlock, 1), new RenderEngine(
				DefaultProps.TEXTURE_PATH_BLOCKS + "/base_stone.png"));
		RenderingEntityBlocks.blockByEntityRenders.put(new EntityRenderIndex(BuildCraftEnergy.engineBlock, 2), new RenderEngine(
				DefaultProps.TEXTURE_PATH_BLOCKS + "/base_iron.png"));

		EnergyProxy.proxy.registerTileEntities();
		EnergyProxy.proxy.registerTextureFX();
	}

	/*
	 * @Override public GuiScreen handleGUI(int i) { TileEngine tile = new
	 * TileEngine();
	 *
	 * switch (Utils.intToPacketId(i)) { case EngineSteamGUI: tile.engine = new
	 * EngineStone(tile); return new GuiSteamEngine(
	 * ModLoader.getMinecraftInstance().thePlayer.inventory, tile); case
	 * EngineCombustionGUI: tile.engine = new EngineIron(tile); return new
	 * GuiCombustionEngine(
	 * ModLoader.getMinecraftInstance().thePlayer.inventory, tile); default:
	 * return null; } }
	 */

}
