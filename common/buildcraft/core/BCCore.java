/* Copyright (c) 2016 AlexIIL and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.core;

import java.io.File;

import net.minecraft.init.Blocks;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.oredict.OreDictionary;

import buildcraft.api.core.BCLog;
import buildcraft.core.list.ListTooltipHandler;
import buildcraft.core.marker.PathCache;
import buildcraft.core.marker.VolumeCache;
import buildcraft.lib.BCLib;
import buildcraft.lib.BCLibItems;
import buildcraft.lib.marker.MarkerCache;
import buildcraft.lib.registry.CreativeTabManager;
import buildcraft.lib.registry.RegistryHelper;

//@formatter:off
@Mod(modid = BCCore.MODID,
     name = "BuildCraft Core",
     version = BCLib.VERSION,
     acceptedMinecraftVersions = "[1.10.2]",
     dependencies = "required-after:buildcraftlib",
     guiFactory = "buildcraft.core.config.ConfigManager")
//@formatter:on
public class BCCore {
    public static final String MODID = "buildcraftcore";

    @Mod.Instance(MODID)
    public static BCCore INSTANCE = null;

    static {
        BCLibItems.enableGuide();
        BCLibItems.enableDebugger();
    }

    @Mod.EventHandler
    public static void preInit(FMLPreInitializationEvent evt) {
        BCLog.logger.info("");
        BCLog.logger.info("Starting BuildCraft " + BCLib.VERSION);
        BCLog.logger.info("Copyright (c) the BuildCraft team, 2011-2016");
        BCLog.logger.info("http://www.mod-buildcraft.com");
        BCLog.logger.info("");

        File cfgFolder = evt.getModConfigurationDirectory();
        cfgFolder = new File(cfgFolder, "buildcraft");
        RegistryHelper.setRegistryConfig(MODID, new File(cfgFolder, "objects.cfg"));

        BCCoreConfig.preInit(cfgFolder);
        BCCoreProxy.getProxy().fmlPreInit();

        CreativeTabManager.createTab("buildcraft.main");

        BCCoreItems.preInit();
        BCCoreBlocks.preInit();
        BCCoreTriggers.preInit();

        CreativeTabManager.setItem("buildcraft.main", BCCoreItems.wrench);

        NetworkRegistry.INSTANCE.registerGuiHandler(INSTANCE, BCCoreProxy.getProxy());

        MinecraftForge.EVENT_BUS.register(ListTooltipHandler.INSTANCE);

        OreDictionary.registerOre("craftingTableWood", Blocks.CRAFTING_TABLE);
    }

    @Mod.EventHandler
    public static void init(FMLInitializationEvent evt) {
        BCCoreProxy.getProxy().fmlInit();

        BCCoreRecipes.init();
        BCAchievements.init();

        MarkerCache.registerCache(VolumeCache.INSTANCE);
        MarkerCache.registerCache(PathCache.INSTANCE);
    }

    @Mod.EventHandler
    public static void postInit(FMLPostInitializationEvent evt) {
        BCCoreProxy.getProxy().fmlPostInit();
    }
}
