package buildcraft.transport;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;

import buildcraft.core.BCCore;
import buildcraft.lib.BCLib;
import buildcraft.lib.registry.CreativeTabManager;
import buildcraft.lib.registry.RegistryHelper;

@Mod(modid = BCTransport.MODID, name = "BuildCraft Transport", dependencies = "required-after:buildcraftcore", version = BCLib.VERSION)
public class BCTransport {
    public static final String MODID = "buildcrafttransport";

    @Mod.Instance(MODID)
    public static BCTransport INSTANCE = null;

    @Mod.EventHandler
    public static void preInit(FMLPreInitializationEvent evt) {
        RegistryHelper.useOtherModConfigFor(MODID, BCCore.MODID);

        CreativeTabManager.createTab("buildcraft.pipe");

        BCTransportItems.preInit();
        BCTransportBlocks.preInit();
        BCTransportPipes.preInit();

        // CreativeTabManager.setItem("buildcraft.pipe", BCCoreItems.wrench);
        NetworkRegistry.INSTANCE.registerGuiHandler(INSTANCE, BCTransportProxy.getProxy());

        BCTransportProxy.getProxy().fmlPreInit();
    }

    @Mod.EventHandler
    public static void init(FMLInitializationEvent evt) {
        BCTransportProxy.getProxy().fmlInit();
        BCTransportRecipes.init();
    }

    @Mod.EventHandler
    public static void postInit(FMLPostInitializationEvent evt) {

    }
}
