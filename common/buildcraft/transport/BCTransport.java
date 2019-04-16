/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport;

import java.util.function.Consumer;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLInterModComms.IMCEvent;
import net.minecraftforge.fml.common.event.FMLInterModComms.IMCMessage;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;

import buildcraft.api.BCModules;
import buildcraft.api.core.BCLog;
import buildcraft.api.facades.FacadeAPI;
import buildcraft.api.schematics.SchematicBlockFactoryRegistry;

import buildcraft.lib.BCLib;
import buildcraft.lib.config.EnumRestartRequirement;
import buildcraft.lib.registry.CreativeTabManager;
import buildcraft.lib.registry.CreativeTabManager.CreativeTabBC;
import buildcraft.lib.registry.RegistryConfig;
import buildcraft.lib.registry.TagManager;
import buildcraft.lib.registry.TagManager.EnumTagType;
import buildcraft.lib.registry.TagManager.TagEntry;

import buildcraft.core.BCCore;
import buildcraft.silicon.plug.FacadeStateManager;
import buildcraft.transport.net.PipeItemMessageQueue;
import buildcraft.transport.pipe.SchematicBlockPipe;

//@formatter:off
@Mod(
    modid = BCTransport.MODID,
    name = "BuildCraft Transport",
    version = BCLib.VERSION,
    dependencies = "required-after:buildcraftcore@[" + BCLib.VERSION + "]"
)
//@formatter:on
public class BCTransport {
    public static final String MODID = "buildcrafttransport";

    @Mod.Instance(MODID)
    public static BCTransport INSTANCE = null;

    private static CreativeTabBC tabPipes;
    private static CreativeTabBC tabPlugs;

    @Mod.EventHandler
    public static void preInit(FMLPreInitializationEvent evt) {
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
        BCTransportConfig.reloadConfig(EnumRestartRequirement.GAME);

        tabPipes.setItem(BCTransportItems.pipeItemDiamond);
        tabPlugs.setItem(BCTransportItems.plugBlocker);

        NetworkRegistry.INSTANCE.registerGuiHandler(INSTANCE, BCTransportProxy.getProxy());

        SchematicBlockFactoryRegistry.registerFactory("pipe", 300, SchematicBlockPipe::predicate,
            SchematicBlockPipe::new);

        BCTransportProxy.getProxy().fmlPreInit();

        MinecraftForge.EVENT_BUS.register(BCTransportEventDist.INSTANCE);
    }

    @Mod.EventHandler
    public static void init(FMLInitializationEvent evt) {
        BCTransportProxy.getProxy().fmlInit();
        BCTransportRegistries.init();
    }

    // How much time we wasted during a tantrum
    // We ensure that this never exceeds 15 seconds, even if we receive over a million invalid IMC messages
    private static int totalTantrumTime;

    @Mod.EventHandler
    public static void onImcEvent(IMCEvent imc) {
        for (IMCMessage message : imc.getMessages()) {
            if (FacadeAPI.isFacadeMessageId(message.key)) {
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
                    FacadeStateManager.receiveInterModComms(message);
                }
            }
        }
    }

    @Mod.EventHandler
    public static void postInit(FMLPostInitializationEvent evt) {
        BCTransportProxy.getProxy().fmlPostInit();
    }

    static {
        startBatch();
        // Items
        registerTag("item.waterproof").reg("waterproof").locale("pipeWaterproof").oldReg("pipeWaterproof")
            .model("waterproof");
        registerTag("item.plug.blocker").reg("plug_blocker").locale("PipePlug").model("plug_blocker")
            .tab("buildcraft.plugs");
        registerTag("item.plug.power_adaptor").reg("plug_power_adaptor").locale("PipePowerAdapter")
            .model("plug_power_adaptor").tab("buildcraft.plugs");
        registerTag("item.wire").reg("wire").locale("pipeWire").model("wire/").tab("buildcraft.plugs");
        // Pipes
        startBatch();// Pipes
        registerTag("item.pipe.buildcrafttransport.structure").reg("pipe_structure").locale("PipeStructureCobblestone");
        registerTag("item.pipe.buildcrafttransport.wood_item").reg("pipe_wood_item").locale("PipeItemsWood");
        registerTag("item.pipe.buildcrafttransport.wood_fluid").reg("pipe_wood_fluid").locale("PipeFluidsWood");
        registerTag("item.pipe.buildcrafttransport.wood_power").reg("pipe_wood_power").locale("PipePowerWood");
        registerTag("item.pipe.buildcrafttransport.stone_item").reg("pipe_stone_item").locale("PipeItemsStone");
        registerTag("item.pipe.buildcrafttransport.stone_fluid").reg("pipe_stone_fluid").locale("PipeFluidsStone");
        registerTag("item.pipe.buildcrafttransport.stone_power").reg("pipe_stone_power").locale("PipePowerStone");
        registerTag("item.pipe.buildcrafttransport.cobblestone_item").reg("pipe_cobble_item")
            .locale("PipeItemsCobblestone");
        registerTag("item.pipe.buildcrafttransport.cobblestone_fluid").reg("pipe_cobble_fluid")
            .locale("PipeFluidsCobblestone");
        registerTag("item.pipe.buildcrafttransport.cobblestone_power").reg("pipe_cobble_power")
            .locale("PipePowerCobblestone");
        registerTag("item.pipe.buildcrafttransport.quartz_item").reg("pipe_quartz_item").locale("PipeItemsQuartz");
        registerTag("item.pipe.buildcrafttransport.quartz_fluid").reg("pipe_quartz_fluid").locale("PipeFluidsQuartz");
        registerTag("item.pipe.buildcrafttransport.quartz_power").reg("pipe_quartz_power").locale("PipePowerQuartz");
        registerTag("item.pipe.buildcrafttransport.gold_item").reg("pipe_gold_item").locale("PipeItemsGold");
        registerTag("item.pipe.buildcrafttransport.gold_fluid").reg("pipe_gold_fluid").locale("PipeFluidsGold");
        registerTag("item.pipe.buildcrafttransport.gold_power").reg("pipe_gold_power").locale("PipePowerGold");
        registerTag("item.pipe.buildcrafttransport.sandstone_item").reg("pipe_sandstone_item")
            .locale("PipeItemsSandstone");
        registerTag("item.pipe.buildcrafttransport.sandstone_fluid").reg("pipe_sandstone_fluid")
            .locale("PipeFluidsSandstone");
        registerTag("item.pipe.buildcrafttransport.sandstone_power").reg("pipe_sandstone_power")
            .locale("PipePowerSandstone");
        registerTag("item.pipe.buildcrafttransport.iron_item").reg("pipe_iron_item").locale("PipeItemsIron");
        registerTag("item.pipe.buildcrafttransport.iron_fluid").reg("pipe_iron_fluid").locale("PipeFluidsIron");
        registerTag("item.pipe.buildcrafttransport.iron_power").reg("pipe_iron_power").locale("PipePowerIron");
        registerTag("item.pipe.buildcrafttransport.diamond_item").reg("pipe_diamond_item").locale("PipeItemsDiamond");
        registerTag("item.pipe.buildcrafttransport.diamond_fluid").reg("pipe_diamond_fluid")
            .locale("PipeFluidsDiamond");
        registerTag("item.pipe.buildcrafttransport.diamond_power").reg("pipe_diamond_power").locale("PipePowerDiamond");
        registerTag("item.pipe.buildcrafttransport.diamond_wood_item").reg("pipe_diamond_wood_item")
            .locale("PipeItemsWoodenDiamond");
        registerTag("item.pipe.buildcrafttransport.diamond_wood_fluid").reg("pipe_diamond_wood_fluid")
            .locale("PipeFluidsWoodenDiamond");
        registerTag("item.pipe.buildcrafttransport.diamond_wood_power").reg("pipe_diamond_wood_power")
            .locale("PipePowerEmerald");
        registerTag("item.pipe.buildcrafttransport.clay_item").reg("pipe_clay_item").locale("PipeItemsClay");
        registerTag("item.pipe.buildcrafttransport.clay_fluid").reg("pipe_clay_fluid").locale("PipeFluidsClay");
        registerTag("item.pipe.buildcrafttransport.void_item").reg("pipe_void_item").locale("PipeItemsVoid");
        registerTag("item.pipe.buildcrafttransport.void_fluid").reg("pipe_void_fluid").locale("PipeFluidsVoid");
        registerTag("item.pipe.buildcrafttransport.obsidian_item").reg("pipe_obsidian_item")
            .locale("PipeItemsObsidian");
        registerTag("item.pipe.buildcrafttransport.obsidian_fluid").reg("pipe_obsidian_fluid")
            .locale("PipeFluidsObsidian");
        registerTag("item.pipe.buildcrafttransport.lapis_item").reg("pipe_lapis_item").locale("PipeItemsLapis");
        registerTag("item.pipe.buildcrafttransport.daizuli_item").reg("pipe_daizuli_item").locale("PipeItemsDaizuli");
        registerTag("item.pipe.buildcrafttransport.emzuli_item").reg("pipe_emzuli_item").locale("PipeItemsEmzuli");
        registerTag("item.pipe.buildcrafttransport.stripes_item").reg("pipe_stripes_item").locale("PipeItemsStripes");

        registerTag("item.pipe.buildcrafttransport.wood_power_2").reg("pipe_wood_power_2").locale("PipePowerWood2");
        registerTag("item.pipe.buildcrafttransport.quartz_power_2").reg("pipe_quartz_power_2")
            .locale("PipePowerQuartz2");
        endBatch(TagManager.setTab("buildcraft.pipes"));
        // Item Blocks
        registerTag("item.block.filtered_buffer").reg("filtered_buffer").locale("filteredBufferBlock")
            .model("filtered_buffer");
        // Blocks
        registerTag("block.filtered_buffer").reg("filtered_buffer").oldReg("filteredBufferBlock")
            .locale("filteredBufferBlock").model("filtered_buffer");
        registerTag("block.pipe_holder").reg("pipe_holder").locale("pipeHolder");
        // Tiles
        registerTag("tile.filtered_buffer").reg("filtered_buffer");
        registerTag("tile.pipe_holder").reg("pipe_holder");

        endBatch(TagManager.prependTags("buildcrafttransport:", EnumTagType.REGISTRY_NAME, EnumTagType.MODEL_LOCATION)
            .andThen(TagManager.setTab("buildcraft.main")));
    }

    private static TagEntry registerTag(String id) {
        return TagManager.registerTag(id);
    }

    private static void startBatch() {
        TagManager.startBatch();
    }

    private static void endBatch(Consumer<TagEntry> consumer) {
        TagManager.endBatch(consumer);
    }
}
