package buildcraft.factory;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;

import buildcraft.core.BuildCraftCore;
import buildcraft.lib.BuildCraftLib;
import buildcraft.lib.RegistryHelper;

@Mod(modid = BuildCraftFactory.MODID, name = "BuildCraft Factory", dependencies = "required-after:buildcraftcore", version = BuildCraftLib.VERSION)
public class BuildCraftFactory {
    public static final String MODID = "buildcraftfactory";

    @Mod.Instance(MODID)
    public static BuildCraftFactory INSTANCE = null;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent evt) {
        RegistryHelper.useOtherModConfigFor(MODID, BuildCraftCore.MODID);

        BCFactoryItems.preInit();
        BCFactoryBlocks.preInit();

        NetworkRegistry.INSTANCE.registerGuiHandler(INSTANCE, FactoryProxy_BC8.getProxy());
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent evt) {
        FactoryProxy_BC8.getProxy().fmlInit();
        BCFactoryRecipes.init();
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent evt) {

    }
}
