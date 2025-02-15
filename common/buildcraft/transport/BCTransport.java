/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport;

import buildcraft.api.BCModules;
import buildcraft.api.core.BCLog;
import buildcraft.api.facades.FacadeAPI;
import buildcraft.api.imc.BcImcMessage;
import buildcraft.api.schematics.SchematicBlockFactoryRegistry;
import buildcraft.core.BCCore;
import buildcraft.lib.registry.CreativeTabManager;
import buildcraft.lib.registry.CreativeTabManager.CreativeTabBC;
import buildcraft.lib.registry.RegistryConfig;
import buildcraft.lib.registry.TagManager;
import buildcraft.lib.registry.TagManager.EnumTagType;
import buildcraft.lib.registry.TagManager.TagEntry;
import buildcraft.silicon.plug.FacadeStateManager;
import buildcraft.transport.client.TransportItemModelPredicates;
import buildcraft.transport.client.render.PipeTabButton;
import buildcraft.transport.pipe.SchematicBlockPipe;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.*;
import net.minecraftforge.registries.RegisterEvent;

import java.util.function.Consumer;

//@formatter:off
//@Mod(
//    modid = BCTransport.MODID,
//    name = "BuildCraft Transport",
//    version = BCLib.VERSION,
//    dependencies = "required-after:buildcraftcore@[" + BCLib.VERSION + "]"
//)
//@formatter:on
@Mod(BCTransport.MODID)
@Mod.EventBusSubscriber(modid = BCTransport.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class BCTransport {
    public static final String MODID = "buildcrafttransport";

    // @Mod.Instance(MOD_ID)
    public static BCTransport INSTANCE = null;

    private static CreativeTabBC tabPipes;
    private static CreativeTabBC tabPlugs;

    public BCTransport() {
        INSTANCE = this;
    }

    @SubscribeEvent
    public static void preInit(FMLConstructModEvent evt) {
        RegistryConfig.useOtherModConfigFor(MODID, BCCore.MODID);

        tabPipes = CreativeTabManager.createTab("buildcraft.pipes");
        tabPlugs = CreativeTabManager.createTab("buildcraft.plugs");

        BCTransportRegistries.preInit();
        BCTransportConfig.preInit();
        BCTransportBlocks.preInit();
        BCTransportPipes.preInit();
        BCTransportPlugs.preInit();
        BCTransportItems.preInit();
        BCTransportStatements.preInit();

        // Reload after all of the pipe defs have been created.
//        BCTransportConfig.reloadConfig(EnumRestartRequirement.GAME);
        BCTransportConfig.reloadConfig();

        tabPipes.setItemPipe(BCTransportItems.pipeItemDiamond.get(null));
        tabPlugs.setItem(BCTransportItems.plugBlocker);
//
//        NetworkRegistry.INSTANCE.registerGuiHandler(INSTANCE, BCTransportProxy.getProxy());
//
        SchematicBlockFactoryRegistry.registerFactory("pipe", 300, SchematicBlockPipe::predicate,
                SchematicBlockPipe::new);

        BCTransportProxy.getProxy().fmlPreInit();
//
        MinecraftForge.EVENT_BUS.register(BCTransportEventDist.INSTANCE);
    }

    @SubscribeEvent
    public static void init(FMLCommonSetupEvent evt) {
        BCTransportProxy.getProxy().fmlInit();
        BCTransportRegistries.init();
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        ItemBlockRenderTypes.setRenderLayer(BCTransportBlocks.pipeHolder.get(), RenderType.translucent());

        TransportItemModelPredicates.register(event);

        MinecraftForge.EVENT_BUS.register(PipeTabButton.class);
    }

    @SubscribeEvent
    public static void onRegisterEvent(RegisterEvent event) {
        ResourceKey<? extends Registry<?>> registry = event.getRegistryKey();
        if (registry == Registries.CREATIVE_MODE_TAB) {
            // Creative Tab
            Registry.register(event.getVanillaRegistry(), tabPipes.getId(), tabPipes);
            Registry.register(event.getVanillaRegistry(), tabPlugs.getId(), tabPlugs);
        } else if (registry == Registries.BLOCK) {
            // GUI
            BCTransportMenuTypes.registerAll();
        }
    }

    // How much time we wasted during a tantrum
    // We ensure that this never exceeds 15 seconds, even if we receive over a million invalid IMC messages
    private static int totalTantrumTime;

    @SubscribeEvent
//    public static void onImcEvent(IMCEvent imc)
    public static void onImcEvent(InterModProcessEvent imc) {
//        for (InterModComms.IMCMessage message : imc.getMessages())
        InterModComms.getMessages(MODID).forEach(message ->
        {
            if (message.messageSupplier().get() instanceof BcImcMessage bcImcMessage) {
//                if (FacadeAPI.isFacadeMessageId(message.key))
                if (FacadeAPI.isFacadeMessageId(message.method())) {
                    // As this used to be in transport we will need to
                    // pass messages on to silicon

                    // Although we'll make a bit of a fuss about doing so
                    BCLog.logger.warn(
                            "[transport] Recieved a facade IMC message that should be directed to 'buildcraftsilicon' instead!");

                    // a bit bigger fuss
                    new IllegalArgumentException().printStackTrace();

                    {
                        // and a tantrum
                        int time = 1000;
                        if (time + totalTantrumTime > 15000) {
                            time = 0;
                        } else {
                            totalTantrumTime += time;
                            try {
                                Thread.sleep(time);
                            } catch (InterruptedException ignored) {
                                // We don't really care about this error
                            }
                        }
                    }
                    // Ok, tantrum over
                    if (BCModules.SILICON.isLoaded()) {
//                        FacadeStateManager.receiveInterModComms(message);
                        FacadeStateManager.receiveInterModComms(message, bcImcMessage);
                    }
                } else {
                    BCLog.logger.error("[transport.imc] Unknown IMC message type: " + bcImcMessage.getClass().getName());
                }
            }
        });
    }

    @SubscribeEvent
    public static void postInit(FMLLoadCompleteEvent evt) {
        BCTransportConfig.saveConfigs();
        BCTransportProxy.getProxy().fmlPostInit();
        BCTransportConfig.saveConfigs();
    }

    private static final TagManager tagManager = new TagManager();

    static {

        startBatch();
        // Items
//        registerTag("item.waterproof").reg("waterproof").locale("pipeWaterproof").oldReg("pipeWaterproof").model("waterproof");
        registerTag("item.waterproof").reg("waterproof").locale("pipeWaterproof");
//                .model("waterproof");
        registerTag("item.plug.blocker").reg("plug_blocker").locale("PipePlug")
//                .model("plug_blocker")
                .tab("buildcraft.plugs")
        ;
        registerTag("item.plug.power_adaptor").reg("plug_power_adaptor").locale("PipePowerAdapter")
//                .model("plug_power_adaptor")
                .tab("buildcraft.plugs")
        ;
        registerTag("item.wire").reg("wire").locale("pipeWire")
//                .model("wire/")
                .tab("buildcraft.plugs")
        ;

        // Pipes
        startBatch();
        registerTag("item.pipe.buildcrafttransport.structure_cobblestone").reg("pipe_structure_cobblestone").locale("PipeStructureCobblestone");
        registerTag("item.pipe.buildcrafttransport.items_wood").reg("pipe_items_wood").locale("PipeItemsWood");
        registerTag("item.pipe.buildcrafttransport.fluids_wood").reg("pipe_fluids_wood").locale("PipeFluidsWood");
        registerTag("item.pipe.buildcrafttransport.power_wood").reg("pipe_power_wood").locale("PipePowerWood");
        registerTag("item.pipe.buildcrafttransport.items_stone").reg("pipe_items_stone").locale("PipeItemsStone");
        registerTag("item.pipe.buildcrafttransport.fluids_stone").reg("pipe_fluids_stone").locale("PipeFluidsStone");
        registerTag("item.pipe.buildcrafttransport.power_stone").reg("pipe_power_stone").locale("PipePowerStone");
        registerTag("item.pipe.buildcrafttransport.items_cobblestone").reg("pipe_items_cobblestone").locale("PipeItemsCobblestone");
        registerTag("item.pipe.buildcrafttransport.fluids_cobblestone").reg("pipe_fluids_cobblestone").locale("PipeFluidsCobblestone");
        registerTag("item.pipe.buildcrafttransport.power_cobblestone").reg("pipe_power_cobblestone").locale("PipePowerCobblestone");
        registerTag("item.pipe.buildcrafttransport.items_quartz").reg("pipe_items_quartz").locale("PipeItemsQuartz");
        registerTag("item.pipe.buildcrafttransport.fluids_quartz").reg("pipe_fluids_quartz").locale("PipeFluidsQuartz");
        registerTag("item.pipe.buildcrafttransport.power_quartz").reg("pipe_power_quartz").locale("PipePowerQuartz");
        registerTag("item.pipe.buildcrafttransport.items_gold").reg("pipe_items_gold").locale("PipeItemsGold");
        registerTag("item.pipe.buildcrafttransport.fluids_gold").reg("pipe_fluids_gold").locale("PipeFluidsGold");
        registerTag("item.pipe.buildcrafttransport.power_gold").reg("pipe_power_gold").locale("PipePowerGold");
        registerTag("item.pipe.buildcrafttransport.items_sandstone").reg("pipe_items_sandstone").locale("PipeItemsSandstone");
        registerTag("item.pipe.buildcrafttransport.fluids_sandstone").reg("pipe_fluids_sandstone").locale("PipeFluidsSandstone");
        registerTag("item.pipe.buildcrafttransport.power_sandstone").reg("pipe_power_sandstone").locale("PipePowerSandstone");
        registerTag("item.pipe.buildcrafttransport.items_iron").reg("pipe_items_iron").locale("PipeItemsIron");
        registerTag("item.pipe.buildcrafttransport.fluids_iron").reg("pipe_fluids_iron").locale("PipeFluidsIron");
        registerTag("item.pipe.buildcrafttransport.power_iron").reg("pipe_power_iron").locale("PipePowerIron");
        registerTag("item.pipe.buildcrafttransport.items_diamond").reg("pipe_items_diamond").locale("PipeItemsDiamond");
        registerTag("item.pipe.buildcrafttransport.fluids_diamond").reg("pipe_fluids_diamond").locale("PipeFluidsDiamond");
        registerTag("item.pipe.buildcrafttransport.power_diamond").reg("pipe_power_diamond").locale("PipePowerDiamond");
        registerTag("item.pipe.buildcrafttransport.items_diamond_wood").reg("pipe_items_diamond_wood").locale("PipeItemsWoodenDiamond");
        registerTag("item.pipe.buildcrafttransport.fluids_diamond_wood").reg("pipe_fluids_diamond_wood").locale("PipeFluidsWoodenDiamond");
        registerTag("item.pipe.buildcrafttransport.power_diamond_wood").reg("pipe_power_diamond_wood").locale("PipePowerEmerald");
        registerTag("item.pipe.buildcrafttransport.items_clay").reg("pipe_items_clay").locale("PipeItemsClay");
        registerTag("item.pipe.buildcrafttransport.fluids_clay").reg("pipe_fluids_clay").locale("PipeFluidsClay");
        registerTag("item.pipe.buildcrafttransport.items_void").reg("pipe_items_void").locale("PipeItemsVoid");
        registerTag("item.pipe.buildcrafttransport.fluids_void").reg("pipe_fluids_void").locale("PipeFluidsVoid");
        registerTag("item.pipe.buildcrafttransport.items_obsidian").reg("pipe_items_obsidian").locale("PipeItemsObsidian");
        registerTag("item.pipe.buildcrafttransport.fluids_obsidian").reg("pipe_fluids_obsidian").locale("PipeFluidsObsidian");
        registerTag("item.pipe.buildcrafttransport.items_lapis").reg("pipe_items_lapis").locale("PipeItemsLapis");
        registerTag("item.pipe.buildcrafttransport.items_daizuli").reg("pipe_items_daizuli").locale("PipeItemsDaizuli");
        registerTag("item.pipe.buildcrafttransport.items_emzuli").reg("pipe_items_emzuli").locale("PipeItemsEmzuli");
        registerTag("item.pipe.buildcrafttransport.items_stripes").reg("pipe_items_stripes").locale("PipeItemsStripes");

        registerTag("item.pipe.buildcrafttransport.power_wood_2").reg("pipe_power_wood_2").locale("PipePowerWood2");
        registerTag("item.pipe.buildcrafttransport.power_quartz_").reg("pipe_power_quartz_2").locale("PipePowerQuartz2");
        endBatch(TagManager.setTab("buildcraft.pipes"));

        // Item Blocks
//        registerTag("item.block.filtered_buffer").reg("filtered_buffer").locale("filteredBufferBlock").model("filtered_buffer");
        registerTag("item.block.filtered_buffer").reg("filtered_buffer").locale("filteredBufferBlock");
        // Blocks
//        registerTag("block.filtered_buffer").reg("filtered_buffer").oldReg("filteredBufferBlock").locale("filteredBufferBlock").model("filtered_buffer");
        registerTag("block.filtered_buffer").reg("filtered_buffer").locale("filteredBufferBlock");
        registerTag("block.pipe_holder").reg("pipe_holder").locale("pipeHolder");
        // Tiles
        registerTag("tile.filtered_buffer").reg("filtered_buffer");
        registerTag("tile.pipe_holder").reg("pipe_holder");

//        endBatch(TagManager.prependTags("buildcrafttransport:", EnumTagType.REGISTRY_NAME, EnumTagType.MODEL_LOCATION).andThen(TagManager.setTab("buildcraft.main")));
        endBatch(TagManager.prependTags("buildcrafttransport:", EnumTagType.REGISTRY_NAME).andThen(TagManager.setTab("buildcraft.main")));

    }

    private static TagEntry registerTag(String id) {
//        return TagManager.registerTag(id);
        return tagManager.registerTag(id);
    }

    private static void startBatch() {
//        TagManager.startBatch();
        tagManager.startBatch();
    }

    private static void endBatch(Consumer<TagEntry> consumer) {
//        TagManager.endBatch(consumer);
        tagManager.endBatch(consumer);
    }
}
