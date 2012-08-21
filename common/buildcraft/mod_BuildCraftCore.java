/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft;

import java.util.Date;
import java.util.Map;

import net.minecraft.client.Minecraft;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.Init;
import cpw.mods.fml.common.Mod.PostInit;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;

import buildcraft.BuildCraftCore;
import buildcraft.core.ClassMapping;
import buildcraft.core.DefaultProps;
import buildcraft.core.EntityBlock;
import buildcraft.core.EntityEnergyLaser;
import buildcraft.core.EntityLaser;
import buildcraft.core.EntityRobot;
import buildcraft.core.ProxyCore;
import buildcraft.core.network.PacketHandler;
import buildcraft.core.render.RenderEnergyLaser;
import buildcraft.core.render.RenderEntityBlock;
import buildcraft.core.render.RenderLaser;
import buildcraft.core.render.RenderRobot;
import buildcraft.core.utils.Localization;

@Mod(name="BuildCraft", version=DefaultProps.VERSION, useMetadata = false, modid = "BC|CORE")
@NetworkMod(channels = {DefaultProps.NET_CHANNEL_NAME}, packetHandler = PacketHandler.class, clientSideRequired = true, serverSideRequired = true)
public class mod_BuildCraftCore {

	public static mod_BuildCraftCore instance;

	BuildCraftCore proxy = new BuildCraftCore();
	public static boolean initialized = false;

	public mod_BuildCraftCore() {
		instance = this;
	}

	
	@Init
	public void init(FMLInitializationEvent event) {
		BuildCraftCore.load();
	}

	@PostInit
	public void postInit(FMLPostInitializationEvent event) {
		mod_BuildCraftCore.initialize();
		//BuildCraftCore.initializeModel(this);
//		ModLoader.setInGameHook(this, true, true);
	}

	public static void initialize() {
		BuildCraftCore.initialize();

		if (!initialized) {

			// Init rendering if applicable
			ProxyCore.proxy.initializeRendering();
			
			//Initialize localization
			Localization.addLocalization("/lang/buildcraft/", DefaultProps.DEFAULT_LANGUAGE);

			initialized = true;
		}
	}

/*
	 * @Override public void handlePacket(Packet230ModLoader packet) { switch
	 * (PacketIds.values()[packet.packetType]) { case TileDescription:
	 * Utils.handleDescriptionPacket(packet,
	 * ModLoader.getMinecraftInstance().theWorld); break; case TileUpdate:
	 * Utils.handleUpdatePacket(packet,
	 * ModLoader.getMinecraftInstance().theWorld); break;
	 * 
	 * } }
	 */

	@SuppressWarnings({ "rawtypes", "unchecked" })
//	@Override
	public void addRenderer(Map map) {
		map.put(EntityBlock.class, new RenderEntityBlock());
		map.put(EntityLaser.class, new RenderLaser());
		map.put(EntityEnergyLaser.class, new RenderEnergyLaser());
		map.put(EntityRobot.class, new RenderRobot());
	}

	long lastReport = 0;

	//@Override
	public boolean onTickInGame(float f, Minecraft minecraft) {
		if (BuildCraftCore.trackNetworkUsage) {
			Date d = new Date();

			if (d.getTime() - lastReport > 10000) {
				lastReport = d.getTime();
				int bytes = ClassMapping.report();
				System.out.println("BuildCraft bandwidth = " + (bytes / 10) + " bytes / second");
				System.out.println();
			}
		}

		return true;
	}

	/*
	 * @Override public void handlePacket(Packet230ModLoader packet) { switch
	 * (PacketIds.values()[packet.packetType]) { case TileDescription:
	 * Utils.handleDescriptionPacket(packet,
	 * ModLoader.getMinecraftInstance().theWorld); break; case TileUpdate:
	 * Utils.handleUpdatePacket(packet,
	 * ModLoader.getMinecraftInstance().theWorld); break;
	 * 
	 * } }
	 */

}
