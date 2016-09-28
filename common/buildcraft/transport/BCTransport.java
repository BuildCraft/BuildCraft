package buildcraft.transport;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;

import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.core.BCCore;
import buildcraft.lib.BCLib;
import buildcraft.lib.registry.CreativeTabManager;
import buildcraft.lib.registry.RegistryHelper;
import buildcraft.transport.api_move.PipeAPI;
import buildcraft.transport.api_move.PipeFlowType;
import buildcraft.transport.client.model.ModelPipe;
import buildcraft.transport.client.model.ModelPipeItem;
import buildcraft.transport.pipe.PipeRegistry;
import buildcraft.transport.pipe.flow.PipeFlowFluids;
import buildcraft.transport.pipe.flow.PipeFlowItems;
import buildcraft.transport.pipe.flow.PipeFlowPower;
import buildcraft.transport.pipe.flow.PipeFlowStructure;
import buildcraft.transport.plug.PluggableRegistry;

@Mod(modid = BCTransport.MODID, name = "BuildCraft Transport", dependencies = "required-after:buildcraftcore", version = BCLib.VERSION)
public class BCTransport {
    public static final String MODID = "buildcrafttransport";

    @Mod.Instance(MODID)
    public static BCTransport INSTANCE = null;

    @Mod.EventHandler
    public static void preInit(FMLPreInitializationEvent evt) {
        RegistryHelper.useOtherModConfigFor(MODID, BCCore.MODID);

        PipeAPI.pipeRegistry = PipeRegistry.INSTANCE;
        PipeAPI.pluggableRegistry = PluggableRegistry.INSTANCE;

        PipeAPI.flowItems = new PipeFlowType(PipeFlowItems::new, PipeFlowItems::new);
        PipeAPI.flowFluids = new PipeFlowType(PipeFlowFluids::new, PipeFlowFluids::new);
        PipeAPI.flowPower = new PipeFlowType(PipeFlowPower::new, PipeFlowPower::new);
        PipeAPI.flowStructure = new PipeFlowType(PipeFlowStructure::new, PipeFlowStructure::new);

        CreativeTabManager.createTab("buildcraft.pipe");

        BCTransportBlocks.preInit();
        BCTransportPipes.preInit();
        BCTransportPlugs.preInit();
        BCTransportItems.preInit();

        CreativeTabManager.setItem("buildcraft.pipe", BCTransportItems.pipeItemGold);

        // CreativeTabManager.setItem("buildcraft.pipe", BCCoreItems.wrench);
        NetworkRegistry.INSTANCE.registerGuiHandler(INSTANCE, BCTransportProxy.getProxy());

        BCTransportProxy.getProxy().fmlPreInit();

        // TEMP
        MinecraftForge.EVENT_BUS.register(BCTransport.class);
    }

    @Mod.EventHandler
    public static void init(FMLInitializationEvent evt) {
        BCTransportProxy.getProxy().fmlInit();
        BCTransportRecipes.init();
    }

    @Mod.EventHandler
    public static void postInit(FMLPostInitializationEvent evt) {

    }

    // TEMP
    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public static void onModelBake(ModelBakeEvent event) {
        event.getModelRegistry().putObject(new ModelResourceLocation("buildcrafttransport:pipe_holder#normal"), ModelPipe.INSTANCE);
        event.getModelRegistry().putObject(new ModelResourceLocation("buildcrafttransport:pipe_item#inventory"), ModelPipeItem.INSTANCE);
    }
}
