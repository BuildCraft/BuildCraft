package buildcraft.core;

import java.io.File;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;

import buildcraft.api.BCModules;
import buildcraft.api.core.BCLog;
import buildcraft.lib.CreativeTabManager;
import buildcraft.lib.item.ItemBuildCraft_BC8;

@Mod(modid = "buildcraftcore", name = "BuildCraft|Core", version = BCMisc.VERSION)
public class BuildCraftCore {
    @Mod.Instance("buildcraftcore")
    public static BuildCraftCore INSTANCE = null;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent evt) {
        BCLog.logger.info("Starting BuildCraft " + BCMisc.VERSION);
        BCLog.logger.info("Copyright (c) the BuildCraft team, 2011-2016");
        BCLog.logger.info("http://www.mod-buildcraft.com");
        BCModules.init();

        File cfgBase = evt.getModConfigurationDirectory();
        BCRegistry.INSTANCE.setRegistryConfig(new File(cfgBase, "objects.cfg"));

        BCCoreConfig.INSTANCE.preInit();

        CreativeTabManager.createTab("buildcraft.main");

        BCCoreItems.preInit();
        BCCoreBlocks.preInit();

        CreativeTabManager.setItem("buildcraft.main", BCCoreItems.wrench);

        NetworkRegistry.INSTANCE.registerGuiHandler(INSTANCE, CoreProxy.getProxy());
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent evt) {
        CoreProxy.getProxy().fmlInit();
        ItemBuildCraft_BC8.fmlInit();

        BCCoreRecipes.init();
        BCAchievements.init();

        CustomRotationHandlers.init();
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent evt) {

    }
}
