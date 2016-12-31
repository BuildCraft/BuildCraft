package buildcraft.silicon;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;

import buildcraft.core.BCCore;
import buildcraft.lib.BCLib;
import buildcraft.lib.registry.RegistryHelper;

//@formatter:off
@Mod(modid = BCSilicon.MODID,
name = "BuildCraft Silicon",
version = BCLib.VERSION,
dependencies = "required-after:buildcraftcore@[" + BCLib.VERSION + "]")
//@formatter:on
public class BCSilicon {
    public static final String MODID = "buildcraftsilicon";

    @Mod.Instance(MODID)
    public static BCSilicon INSTANCE = null;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent evt) {
        RegistryHelper.useOtherModConfigFor(MODID, BCCore.MODID);

        BCSiliconItems.preInit();
        BCSiliconBlocks.preInit();

        NetworkRegistry.INSTANCE.registerGuiHandler(INSTANCE, BCSiliconProxy.getProxy());

        BCSiliconProxy.getProxy().fmlPreInit();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent evt) {
        BCSiliconProxy.getProxy().fmlInit();
        BCSiliconRecipes.init();
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent evt) {

    }
}
