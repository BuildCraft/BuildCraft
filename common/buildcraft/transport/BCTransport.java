package buildcraft.transport;

import buildcraft.transport.wire.WorldSavedDataWireSystems;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;

import buildcraft.api.transport.ICustomPipeConnection;
import buildcraft.api.transport.PipeConnectionAPI;
import buildcraft.api.transport.neptune.PipeAPI;
import buildcraft.api.transport.neptune.PipeFlowType;

import buildcraft.core.BCCore;
import buildcraft.lib.BCLib;
import buildcraft.lib.config.EnumRestartRequirement;
import buildcraft.lib.registry.CreativeTabManager;
import buildcraft.lib.registry.RegistryHelper;
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

        // CreativeTabManager.setItem("buildcraft.pipe", BCCoreItems.wrench);
        NetworkRegistry.INSTANCE.registerGuiHandler(INSTANCE, BCTransportProxy.getProxy());

        BCTransportProxy.getProxy().fmlPreInit();

        MinecraftForge.EVENT_BUS.register(new Object() {
            @SubscribeEvent
            public void onWorldTick(TickEvent.WorldTickEvent event) {
                WorldSavedDataWireSystems.get(event.world).updateAllWireSystems();
            }

            @SubscribeEvent
            public void onClientTick(TickEvent.ClientTickEvent event) {
                if(Minecraft.getMinecraft().theWorld != null) {
                    WorldSavedDataWireSystems.get(Minecraft.getMinecraft().theWorld).updateAllWireSystems(); // TODO: remove this
                }
            }
        });
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
