/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.core;

import buildcraft.core.list.ListTooltipHandler;
import buildcraft.core.marker.PathCache;
import buildcraft.core.marker.VolumeCache;
import buildcraft.core.marker.volume.MessageVolumeBoxes;
import buildcraft.lib.BCLib;
import buildcraft.lib.BCLibItems;
import buildcraft.lib.BCMessageHandler;
import buildcraft.lib.marker.MarkerCache;
import buildcraft.lib.registry.CreativeTabManager;
import buildcraft.lib.registry.CreativeTabManager.CreativeTabBC;
import net.minecraft.init.Blocks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.oredict.OreDictionary;

import java.io.File;

@Mod(//
        modid = BCCore.MODID,//
        name = "BuildCraft Core",//
        version = BCLib.VERSION,//
        dependencies = "required-after:buildcraftlib@[" + BCLib.VERSION + "]",//
        acceptedMinecraftVersions = "[1.11]",//
        guiFactory = "buildcraft.core.client.ConfigGuiFactoryBC"//
)
public class BCCore {
    public static final String MODID = "buildcraftcore";

    @Mod.Instance(MODID)
    public static BCCore INSTANCE = null;

    static {
        BCLibItems.enableGuide();
        BCLibItems.enableDebugger();
    }

    @Mod.EventHandler
    public static void preInit(FMLPreInitializationEvent event) {
        File cfgFolder = event.getModConfigurationDirectory();
        cfgFolder = new File(cfgFolder, "buildcraft");
        BCCoreConfig.preInit(cfgFolder);
        BCCoreProxy.getProxy().fmlPreInit();

        CreativeTabBC  tab= CreativeTabManager.createTab("buildcraft.main");

        BCCoreItems.preInit();
        BCCoreBlocks.preInit();
        BCCoreStatements.preInit();

        tab.setItem(BCCoreItems.wrench);

        NetworkRegistry.INSTANCE.registerGuiHandler(INSTANCE, BCCoreProxy.getProxy());

        MinecraftForge.EVENT_BUS.register(ListTooltipHandler.INSTANCE);

        OreDictionary.registerOre("craftingTableWood", Blocks.CRAFTING_TABLE);

        MinecraftForge.EVENT_BUS.register(BCCoreEventDist.INSTANCE);
        BCMessageHandler.addMessageType(MessageVolumeBoxes.class, MessageVolumeBoxes.Handler.INSTANCE, Side.CLIENT);
    }

    @Mod.EventHandler
    public static void init(FMLInitializationEvent event) {
        BCCoreProxy.getProxy().fmlInit();

        BCCoreRecipes.init();
        BCAchievements.init();

        MarkerCache.registerCache(VolumeCache.INSTANCE);
        MarkerCache.registerCache(PathCache.INSTANCE);
    }

    @Mod.EventHandler
    public static void postInit(FMLPostInitializationEvent event) {
        BCCoreProxy.getProxy().fmlPostInit();
        BCCoreConfig.postInit();
    }
}
