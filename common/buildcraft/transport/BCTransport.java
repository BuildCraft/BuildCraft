package buildcraft.transport;

import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;

import buildcraft.api.transport.ICustomPipeConnection;
import buildcraft.api.transport.PipeConnectionAPI;
import buildcraft.api.transport.neptune.PipeAPI;
import buildcraft.api.transport.neptune.PipeFlowType;

import buildcraft.core.BCCore;
import buildcraft.lib.BCLib;
import buildcraft.lib.BCMessageHandler;
import buildcraft.lib.config.EnumRestartRequirement;
import buildcraft.lib.registry.CreativeTabManager;
import buildcraft.lib.registry.RegistryHelper;
import buildcraft.transport.pipe.PipeRegistry;
import buildcraft.transport.pipe.flow.PipeFlowFluids;
import buildcraft.transport.pipe.flow.PipeFlowItems;
import buildcraft.transport.pipe.flow.PipeFlowPower;
import buildcraft.transport.pipe.flow.PipeFlowStructure;
import buildcraft.transport.plug.PluggableRegistry;
import buildcraft.transport.wire.MessageWireSystems;
import buildcraft.transport.wire.MessageWireSystemsPowered;

//@formatter:off
@Mod(modid = BCTransport.MODID,
name = "BuildCraft Transport",
version = BCLib.VERSION,
dependencies = "required-after:buildcraftcore@[" + BCLib.VERSION + "]")
//@formatter:on
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
        CreativeTabManager.createTab("buildcraft.gate");

        BCTransportConfig.preInit();
        BCTransportBlocks.preInit();
        BCTransportPipes.preInit();
        BCTransportPlugs.preInit();
        BCTransportItems.preInit();
        BCTransportStatements.preInit();

        ICustomPipeConnection smallerBlockConnection = (world, pos, face, state) -> face == EnumFacing.UP ? 0 : 2 / 16f;
        PipeConnectionAPI.registerConnection(Blocks.CHEST, smallerBlockConnection);
        PipeConnectionAPI.registerConnection(Blocks.TRAPPED_CHEST, smallerBlockConnection);
        PipeConnectionAPI.registerConnection(Blocks.HOPPER, smallerBlockConnection);

        // Reload after all of the pipe defs have been created.
        BCTransportConfig.reloadConfig(EnumRestartRequirement.GAME);

        CreativeTabManager.setItem("buildcraft.pipe", BCTransportItems.pipeItemGold);
        CreativeTabManager.setItem("buildcraft.gate", BCTransportItems.plugGate);

        CreativeTabManager.setItem("buildcraft.pipe", BCTransportItems.pipeItemDiamond);
        NetworkRegistry.INSTANCE.registerGuiHandler(INSTANCE, BCTransportProxy.getProxy());

        BCTransportProxy.getProxy().fmlPreInit();

        MinecraftForge.EVENT_BUS.register(BCTransportEventDist.INSTANCE);
        BCMessageHandler.addMessageType(MessageWireSystems.class, MessageWireSystems.Handler.INSTANCE, Side.CLIENT);
        BCMessageHandler.addMessageType(MessageWireSystemsPowered.class, MessageWireSystemsPowered.Handler.INSTANCE, Side.CLIENT);
    }

    @Mod.EventHandler
    public static void init(FMLInitializationEvent evt) {
        BCTransportProxy.getProxy().fmlInit();
        BCTransportRecipes.init();
    }

    @Mod.EventHandler
    public static void postInit(FMLPostInitializationEvent evt) {
        BCTransportProxy.getProxy().fmlPostInit();
    }
}
