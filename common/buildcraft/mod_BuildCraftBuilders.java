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
import buildcraft.BuildCraftBuilders;
import buildcraft.builders.ClientBuilderHook;
import buildcraft.core.DefaultProps;
import buildcraft.core.network.PacketHandler;


@Mod(name="BuildCraft Builders", version=DefaultProps.VERSION, useMetadata = false, modid = "BC|BUILDERS")
@NetworkMod(channels = {DefaultProps.NET_CHANNEL_NAME}, packetHandler = PacketHandler.class, clientSideRequired = true, serverSideRequired = true)
public class mod_BuildCraftBuilders {

	public static mod_BuildCraftBuilders instance;

	public mod_BuildCraftBuilders() {
		instance = this;
	}

	@Init
	public void init(FMLInitializationEvent event) {
		BuildCraftBuilders.load();
	}

	@PostInit
	public void postInit(FMLPostInitializationEvent event) {
		BuildCraftBuilders.addHook(new ClientBuilderHook());
		BuildCraftBuilders.initialize();
		// CoreProxy.registerGUI(this,
		// Utils.packetIdToInt(PacketIds.FillerGUI));
		// CoreProxy.registerGUI(this,
		// Utils.packetIdToInt(PacketIds.TemplateGUI));
		// CoreProxy.registerGUI(this,
		// Utils.packetIdToInt(PacketIds.BuilderGUI));
	}

	/*
	 * @Override public GuiScreen handleGUI(int i) { switch
	 * (Utils.intToPacketId(i)) { case FillerGUI: return new GuiFiller(
	 * ModLoader.getMinecraftInstance().thePlayer.inventory, new TileFiller());
	 * case TemplateGUI: return new GuiTemplate(
	 * ModLoader.getMinecraftInstance().thePlayer.inventory, new
	 * TileArchitect()); case BuilderGUI: TileBuilder tile = new TileBuilder();
	 * tile.worldObj = ModLoader.getMinecraftInstance().theWorld; return new
	 * GuiBuilder( ModLoader.getMinecraftInstance().thePlayer.inventory, tile);
	 * default: return null; } }
	 */

}
