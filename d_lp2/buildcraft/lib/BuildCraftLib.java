package buildcraft.lib;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import buildcraft.api.BCModules;
import buildcraft.api.core.BuildCraftAPI;
import buildcraft.lib.block.VanillaRotationHandlers;
import buildcraft.lib.item.ItemBuildCraft_BC8;
import buildcraft.lib.list.VanillaListHandlers;
import buildcraft.lib.misc.FakePlayerUtil;

//@formatter:off
@Mod(modid = BuildCraftLib.MODID,
      name = "BuildCraft Lib",
      version = BuildCraftLib.VERSION,
      acceptedMinecraftVersions = "[1.9]",
      dependencies = "required-after:Forge@[12.16.0.1865,12.16.1)")
//@formatter:on
public class BuildCraftLib {
    public static final String MODID = "buildcraftlib";
    public static final String VERSION = "@VERSION@";

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent evt) {
        BCModules.fmlPreInit();
        BuildCraftAPI.fakePlayerProvider = FakePlayerUtil.INSTANCE;
        LibProxy.getProxy().fmlPreInit();
        BCMessageHandler.preInit();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent evt) {
        LibProxy.getProxy().fmlInit();

        VanillaRotationHandlers.fmlInit();
        VanillaListHandlers.fmlInit();

        ItemBuildCraft_BC8.fmlInit();

        BCMessageHandler.init();
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent evt) {
        LibProxy.getProxy().fmlPostInit();

        VanillaListHandlers.fmlPostInit();
    }
}
