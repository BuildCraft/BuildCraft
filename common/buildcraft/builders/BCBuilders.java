package buildcraft.builders;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;

import buildcraft.core.BCCore;
import buildcraft.lib.BCLib;
import buildcraft.lib.RegistryHelper;

@Mod(modid = BCBuilders.MODID, name = "BuildCraft Builders", dependencies = "required-after:buildcraftcore", version = BCLib.VERSION)
public class BCBuilders {
    public static final String MODID = "buildcraftbuilders";

    @Mod.Instance(MODID)
    public static BCBuilders INSTANCE = null;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent evt) {
        RegistryHelper.useOtherModConfigFor(MODID, BCCore.MODID);

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
