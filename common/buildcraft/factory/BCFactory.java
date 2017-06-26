/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.factory;

import java.util.function.Consumer;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;

import buildcraft.lib.BCLib;
import buildcraft.lib.registry.RegistryHelper;
import buildcraft.lib.registry.TagManager;
import buildcraft.lib.registry.TagManager.EnumTagType;
import buildcraft.lib.registry.TagManager.TagEntry;
import buildcraft.lib.tile.TileBC_Neptune;

import buildcraft.core.BCCore;
import buildcraft.factory.tile.TileAutoWorkbenchItems;
import buildcraft.factory.tile.TileChute;
import buildcraft.factory.tile.TileDistiller_BC8;
import buildcraft.factory.tile.TileFloodGate;
import buildcraft.factory.tile.TileHeatExchangeEnd;
import buildcraft.factory.tile.TileHeatExchangeStart;
import buildcraft.factory.tile.TileMiningWell;
import buildcraft.factory.tile.TilePump;
import buildcraft.factory.tile.TileTank;

//@formatter:off
@Mod(modid = BCFactory.MODID,
name = "BuildCraft Factory",
version = BCLib.VERSION,
dependencies = "required-after:buildcraftcore@[" + BCLib.VERSION + "]")
//@formatter:on
public class BCFactory {
    public static final String MODID = "buildcraftfactory";

    @Mod.Instance(MODID)
    public static BCFactory INSTANCE = null;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent evt) {
        RegistryHelper.useOtherModConfigFor(MODID, BCCore.MODID);

        NetworkRegistry.INSTANCE.registerGuiHandler(INSTANCE, BCFactoryProxy.getProxy());

        MinecraftForge.EVENT_BUS.register(BCFactoryEventDist.INSTANCE);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent evt) {
        BCFactoryProxy.getProxy().fmlInit();
        TileBC_Neptune.registerTile(TileAutoWorkbenchItems.class, "tile.autoworkbench.item");
        TileBC_Neptune.registerTile(TileMiningWell.class, "tile.mining_well");
        TileBC_Neptune.registerTile(TilePump.class, "tile.pump");
        TileBC_Neptune.registerTile(TileFloodGate.class, "tile.flood_gate");
        TileBC_Neptune.registerTile(TileTank.class, "tile.tank");
        TileBC_Neptune.registerTile(TileChute.class, "tile.chute");
        TileBC_Neptune.registerTile(TileDistiller_BC8.class, "tile.distiller");
        if (BCLib.DEV) {
            TileBC_Neptune.registerTile(TileHeatExchangeStart.class, "tile.heat_exchange.start");
            TileBC_Neptune.registerTile(TileHeatExchangeEnd.class, "tile.heat_exchange.end");
        }
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent evt) {

    }

    static {

        startBatch();// factory
        // BC Factory Items
        registerTag("item.plastic.sheet").reg("plastic_sheet").locale("plasticSheet").oldReg("plasticSheet").model("plastic_sheet");
        registerTag("item.water_gel_spawn").reg("water_gel_spawn").locale("waterGel").model("water_gel");
        registerTag("item.gel").reg("gel").locale("gel").model("gel");
        // BC Factory Item Blocks
        registerTag("item.block.plastic").reg("plastic_block").locale("plasticBlock").model("plastic_block/");
        registerTag("item.block.autoworkbench.item").reg("autoworkbench_item").locale("autoWorkbenchBlock").model("autoworkbench_item");
        registerTag("item.block.mining_well").reg("mining_well").locale("miningWellBlock").model("mining_well");
        registerTag("item.block.pump").reg("pump").locale("pumpBlock").model("pump");
        registerTag("item.block.flood_gate").reg("flood_gate").locale("floodGateBlock").model("flood_gate");
        registerTag("item.block.tank").reg("tank").locale("tankBlock").model("tank");
        registerTag("item.block.chute").reg("chute").locale("chuteBlock").model("chute");
        registerTag("item.block.distiller").reg("distiller").locale("distiller").model("distiller");
        registerTag("item.block.heat_exchange.start").reg("heat_exchange_start").locale("heat_exchange_start").model("heat_exchange_start");
        registerTag("item.block.heat_exchange.middle").reg("heat_exchange_middle").locale("heat_exchange_middle").model("heat_exchange_middle");
        registerTag("item.block.heat_exchange.end").reg("heat_exchange_end").locale("heat_exchange_end").model("heat_exchange_end");
        // BC Factory Blocks
        registerTag("block.plastic").reg("plastic").locale("plasticBlock").model("plastic");
        registerTag("block.autoworkbench.item").reg("autoworkbench_item").oldReg("autoWorkbenchBlock").locale("autoWorkbenchBlock").model("autoworkbench_item");
        registerTag("block.mining_well").reg("mining_well").oldReg("miningWellBlock").locale("miningWellBlock").model("mining_well");
        registerTag("block.pump").reg("pump").oldReg("pumpBlock").locale("pumpBlock").model("pump");
        registerTag("block.tube").reg("tube").oldReg("tubeBlock").locale("tubeBlock").model("tube");
        registerTag("block.flood_gate").reg("flood_gate").oldReg("floodGateBlock").locale("floodGateBlock").model("flood_gate");
        registerTag("block.tank").reg("tank").oldReg("tankBlock").locale("tankBlock").model("tank");
        registerTag("block.chute").reg("chute").oldReg("chuteBlock").locale("chuteBlock").model("chute");
        registerTag("block.water_gel").reg("water_gel").locale("waterGel").model("water_gel");
        registerTag("block.distiller").reg("distiller").locale("distiller").model("distiller");
        registerTag("block.heat_exchange.start").reg("heat_exchange_start").locale("heat_exchange_start").model("heat_exchange_start");
        registerTag("block.heat_exchange.middle").reg("heat_exchange_middle").locale("heat_exchange_middle").model("heat_exchange_middle");
        registerTag("block.heat_exchange.end").reg("heat_exchange_end").locale("heat_exchange_end").model("heat_exchange_end");
        // BC Factory Tiles
        registerTag("tile.autoworkbench.item").reg("autoworkbench_item");
        registerTag("tile.mining_well").reg("mining_well");
        registerTag("tile.pump").reg("pump");
        registerTag("tile.flood_gate").reg("flood_gate");
        registerTag("tile.tank").reg("tank");
        registerTag("tile.chute").reg("chute");
        registerTag("tile.distiller").reg("distiller");
        registerTag("tile.heat_exchange.start").reg("heat_exchange.start");
        registerTag("tile.heat_exchange.middle").reg("heat_exchange.middle");
        registerTag("tile.heat_exchange.end").reg("heat_exchange.end");

        endBatch(TagManager.prependTags("buildcraftfactory:", EnumTagType.REGISTRY_NAME, EnumTagType.MODEL_LOCATION).andThen(TagManager.setTab("buildcraft.main")));
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
