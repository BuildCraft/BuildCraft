package buildcraft.core;

import java.io.File;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;

import buildcraft.api.core.BCLog;
import buildcraft.core.list.ListTooltipHandler;
import buildcraft.lib.BuildCraftLib;
import buildcraft.lib.CreativeTabManager;
import buildcraft.lib.RegistryHelper;

//@formatter:off
@Mod(modid = BuildCraftCore.MODID,
        name = "BuildCraft|Core",
        version = BCMisc.VERSION,
        acceptedMinecraftVersions = "[1.9]",
        dependencies = "required-after:Forge@[12.16.0.1865,12.16.1)",
        guiFactory = "buildcraft.core.config.ConfigManager")
//@formatter:on
public class BuildCraftCore {
    public static final String MODID = "buildcraftcore";

    @Mod.Instance(MODID)
    public static BuildCraftCore INSTANCE = null;

    public static SimpleNetworkWrapper netWrapper;

    static {
        BuildCraftLib.staticInit();
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent evt) {
        BuildCraftLib.fmlPreInit();

        BCLog.logger.info("");
        BCLog.logger.info("Starting BuildCraft " + BCMisc.VERSION);
        BCLog.logger.info("Copyright (c) the BuildCraft team, 2011-2016");
        BCLog.logger.info("http://www.mod-buildcraft.com");
        BCLog.logger.info("");

        File cfgBase = evt.getModConfigurationDirectory();
        RegistryHelper.setRegistryConfig(MODID, new File(cfgBase, "objects.cfg"));

        BCCoreConfig.preInit(new File(cfgBase, "main.cfg"));

        CreativeTabManager.createTab("buildcraft.main");

        BCCoreItems.preInit();
        BCCoreBlocks.preInit();

        CreativeTabManager.setItem("buildcraft.main", BCCoreItems.wrench);

        NetworkRegistry.INSTANCE.registerGuiHandler(INSTANCE, CoreProxy.getProxy());

        MinecraftForge.EVENT_BUS.register(ListTooltipHandler.INSTANCE);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent evt) {
        BuildCraftLib.fmlInit();

        CoreProxy.getProxy().fmlInit();

        BCCoreRecipes.init();
        BCAchievements.init();
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent evt) {
        BuildCraftLib.fmlPostInit();
    }
}
