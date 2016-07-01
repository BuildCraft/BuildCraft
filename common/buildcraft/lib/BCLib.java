/* Copyright (c) 2016 AlexIIL and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.lib;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;

import buildcraft.api.BCModules;
import buildcraft.api.core.BuildCraftAPI;
import buildcraft.lib.block.VanillaRotationHandlers;
import buildcraft.lib.bpt.vanilla.VanillaBlueprints;
import buildcraft.lib.item.ItemManager;
import buildcraft.lib.list.VanillaListHandlers;
import buildcraft.lib.marker.MarkerCache;
import buildcraft.lib.misc.FakePlayerUtil;

//@formatter:off
@Mod(modid = BCLib.MODID,
     name = "BuildCraft Lib",
     version = BCLib.VERSION,
     acceptedMinecraftVersions = "[1.9.4]",
     dependencies = "required-after:Forge@[12.17.0.1909,)")
//@formatter:on
public class BCLib {
    public static final String MODID = "buildcraftlib";
    public static final String VERSION = "@VERSION@";

    @Instance(MODID)
    public static BCLib INSTANCE;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent evt) {
        BCModules.fmlPreInit();
        BuildCraftAPI.fakePlayerProvider = FakePlayerUtil.INSTANCE;
        LibProxy.getProxy().fmlPreInit();

        BCLibItems.preInit();

        BCMessageHandler.fmlPreInit();
        NetworkRegistry.INSTANCE.registerGuiHandler(INSTANCE, LibProxy.getProxy());

        MinecraftForge.EVENT_BUS.register(BCLibEventDist.INSTANCE);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent evt) {
        LibProxy.getProxy().fmlInit();

        VanillaRotationHandlers.fmlInit();
        VanillaListHandlers.fmlInit();
        VanillaBlueprints.fmlInit();

        ItemManager.fmlInit();

        BCLibDatabase.fmlInit();
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent evt) {
        LibProxy.getProxy().fmlPostInit();
        BCMessageHandler.fmlPostInit();
        VanillaListHandlers.fmlPostInit();
        MarkerCache.postInit();
    }

    @Mod.EventHandler
    public void onServerStarted(FMLServerStartedEvent evt) {
        BCLibEventDist.onServerStarted(evt);
    }
}
