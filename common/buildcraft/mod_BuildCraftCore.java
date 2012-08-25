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

import net.minecraft.client.Minecraft;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.Init;
import cpw.mods.fml.common.Mod.PostInit;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.registry.EntityRegistry;

import buildcraft.BuildCraftCore;
import buildcraft.core.ClassMapping;
import buildcraft.core.DefaultProps;
import buildcraft.core.EntityEnergyLaser;
import buildcraft.core.EntityLaser;
import buildcraft.core.EntityRobot;
import buildcraft.core.ProxyCore;
import buildcraft.core.network.EntityIds;
import buildcraft.core.network.PacketHandler;
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
//		ModLoader.setInGameHook(this, true, true);
	}

	public static void initialize() {
		BuildCraftCore.initialize();

		if (!initialized) {

			EntityRegistry.registerModEntity(EntityRobot.class, "bcRobot", EntityIds.ROBOT, instance, 50, 1, true);
			EntityRegistry.registerModEntity(EntityLaser.class, "bcLaser", EntityIds.LASER, instance, 50, 1, true);
			EntityRegistry.registerModEntity(EntityEnergyLaser.class, "bcEnergyLaser", EntityIds.ENERGY_LASER, instance, 50, 1, true);
			
			// Init rendering if applicable
			ProxyCore.proxy.initializeRendering();
			ProxyCore.proxy.initializeEntityRendering();
			
			//Initialize localization
			Localization.addLocalization("/lang/buildcraft/", DefaultProps.DEFAULT_LANGUAGE);

			initialized = true;
		}
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

}
