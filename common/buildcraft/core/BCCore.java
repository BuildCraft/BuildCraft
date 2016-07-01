/* Copyright (c) 2016 AlexIIL and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.core;

import java.io.File;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;

import buildcraft.api.core.BCLog;
import buildcraft.core.list.ListTooltipHandler;
import buildcraft.core.marker.PathCache;
import buildcraft.core.marker.VolumeCache;
import buildcraft.lib.BCLib;
import buildcraft.lib.BCLibItems;
import buildcraft.lib.CreativeTabManager;
import buildcraft.lib.RegistryHelper;
import buildcraft.lib.marker.MarkerCache;

//@formatter:off
@Mod(modid = BCCore.MODID,
     name = "BuildCraft Core",
     version = BCLib.VERSION,
     acceptedMinecraftVersions = "[1.9.4]",
     dependencies = "required-after:buildcraftlib",
     guiFactory = "buildcraft.core.config.ConfigManager")
//@formatter:on
public class BCCore {
    public static final String MODID = "buildcraftcore";

    @Mod.Instance(MODID)
    public static BCCore INSTANCE = null;

    static {
        BCLibItems.enableGuide();
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent evt) {
        BCLog.logger.info("");
        BCLog.logger.info("Starting BuildCraft " + BCLib.VERSION);
        BCLog.logger.info("Copyright (c) the BuildCraft team, 2011-2016");
        BCLog.logger.info("http://www.mod-buildcraft.com");
        BCLog.logger.info("");

        File cfgBase = evt.getModConfigurationDirectory();
        RegistryHelper.setRegistryConfig(MODID, new File(cfgBase, "objects.cfg"));

        BCCoreConfig.preInit(cfgBase);
        CoreProxy.getProxy().fmlPreInit();

        CreativeTabManager.createTab("buildcraft.main");

        BCCoreItems.preInit();
        BCCoreBlocks.preInit();
        BCCoreTriggers.preInit();

        CreativeTabManager.setItem("buildcraft.main", BCCoreItems.wrench);

        NetworkRegistry.INSTANCE.registerGuiHandler(INSTANCE, CoreProxy.getProxy());

        MinecraftForge.EVENT_BUS.register(ListTooltipHandler.INSTANCE);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent evt) {
        CoreProxy.getProxy().fmlInit();

        BCCoreRecipes.init();
        BCAchievements.init();

        MarkerCache.registerCache(VolumeCache.INSTANCE);
        MarkerCache.registerCache(PathCache.INSTANCE);
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent evt) {
        CoreProxy.getProxy().fmlPostInit();
    }
}
