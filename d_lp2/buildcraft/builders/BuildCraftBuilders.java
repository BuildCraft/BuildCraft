package buildcraft.builders;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;

import buildcraft.core.BuildCraftCore;
import buildcraft.lib.BuildCraftLib;
import buildcraft.lib.RegistryHelper;

@Mod(modid = BuildCraftBuilders.MODID, name = "BuildCraft Builders", dependencies = "required-after:buildcraftcore", version = BuildCraftLib.VERSION)
public class BuildCraftBuilders {
    public static final String MODID = "buildcraftbuilders";

    @Mod.Instance(MODID)
    public static BuildCraftBuilders INSTANCE = null;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent evt) {
        RegistryHelper.useOtherModConfigFor(MODID, BuildCraftCore.MODID);

        BCBuildersItems.preInit();
        BCBuildersBlocks.preInit();

        NetworkRegistry.INSTANCE.registerGuiHandler(INSTANCE, BuildersProxy_BC8.getProxy());
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent evt) {
        BuildersProxy_BC8.getProxy().fmlInit();
        BCBuildersRecipes.init();
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent evt) {

    }
}
