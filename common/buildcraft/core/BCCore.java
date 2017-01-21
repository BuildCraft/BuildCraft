/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.core;

import java.io.File;
import java.util.function.Consumer;

import net.minecraft.init.Blocks;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.oredict.OreDictionary;

import buildcraft.core.list.ListTooltipHandler;
import buildcraft.core.marker.PathCache;
import buildcraft.core.marker.VolumeCache;
import buildcraft.lib.BCLib;
import buildcraft.lib.BCLibItems;
import buildcraft.lib.marker.MarkerCache;
import buildcraft.lib.registry.CreativeTabManager;
import buildcraft.lib.registry.CreativeTabManager.CreativeTabBC;
import buildcraft.lib.registry.TagManager;
import buildcraft.lib.registry.TagManager.EnumTagType;
import buildcraft.lib.registry.TagManager.TagEntry;

@Mod(//
    modid = BCCore.MODID,//
    name = "BuildCraft Core",//
    version = BCLib.VERSION,//
    dependencies = "required-after:buildcraftlib@[" + BCLib.VERSION + "]",//
    guiFactory = "buildcraft.core.client.ConfigGuiFactoryBC"//
)
public class BCCore {
    public static final String MODID = "buildcraftcore";

    @Mod.Instance(MODID)
    public static BCCore INSTANCE = null;

    static {
        BCLibItems.enableGuide();
        BCLibItems.enableDebugger();
    }

    @Mod.EventHandler
    public static void preInit(FMLPreInitializationEvent event) {
        File cfgFolder = event.getModConfigurationDirectory();
        cfgFolder = new File(cfgFolder, "buildcraft");
        BCCoreConfig.preInit(cfgFolder);
        BCCoreProxy.getProxy().fmlPreInit();

        CreativeTabBC tab = CreativeTabManager.createTab("buildcraft.main");

        BCCoreItems.preInit();
        BCCoreBlocks.preInit();
        BCCoreStatements.preInit();

        tab.setItem(BCCoreItems.wrench);

        NetworkRegistry.INSTANCE.registerGuiHandler(INSTANCE, BCCoreProxy.getProxy());

        MinecraftForge.EVENT_BUS.register(ListTooltipHandler.INSTANCE);

        OreDictionary.registerOre("craftingTableWood", Blocks.CRAFTING_TABLE);
    }

    @Mod.EventHandler
    public static void init(FMLInitializationEvent event) {
        BCCoreProxy.getProxy().fmlInit();

        BCCoreRecipes.init();
        BCAchievements.init();

        MarkerCache.registerCache(VolumeCache.INSTANCE);
        MarkerCache.registerCache(PathCache.INSTANCE);
    }

    @Mod.EventHandler
    public static void postInit(FMLPostInitializationEvent event) {
        BCCoreProxy.getProxy().fmlPostInit();
        BCCoreConfig.postInit();
    }

    static {
        startBatch();
        // Items
        registerTag("item.wrench").reg("wrench").locale("wrenchItem").oldReg("wrenchItem").model("wrench");
        registerTag("item.diamond.shard").reg("diamond_shard").locale("diamondShard").model("diamond_shard").tab("vanilla.materials");
        registerTag("item.gear.wood").reg("gear_wood").locale("woodenGearItem").oreDict("gearWood").oldReg("woodenGearItem").model("gears/wood");
        registerTag("item.gear.stone").reg("gear_stone").locale("stoneGearItem").oreDict("gearStone").oldReg("stoneGearItem").model("gears/stone");
        registerTag("item.gear.iron").reg("gear_iron").locale("ironGearItem").oreDict("gearIron").oldReg("ironGearItem").model("gears/iron");
        registerTag("item.gear.gold").reg("gear_gold").locale("goldGearItem").oreDict("gearGold").oldReg("goldGearItem").model("gears/gold");
        registerTag("item.gear.diamond").reg("gear_diamond").locale("diamondGearItem").oreDict("gearDiamond").oldReg("diamondGearItem").model("gears/diamond");
        registerTag("item.list").reg("list").locale("list").oldReg("listItem").model("list_");
        registerTag("item.map_location").reg("map_location").locale("mapLocation").oldReg("mapLocation").model("map_location/");
        registerTag("item.paintbrush").reg("paintbrush").locale("paintbrush").model("paintbrush/");
        registerTag("item.marker_connector").reg("marker_connector").locale("markerConnector").model("marker_connector");
        registerTag("item.volume_marker").reg("volume_marker").locale("volume_marker").model("volume_marker");
        // Item Blocks
        registerTag("item.block.marker.volume").reg("marker_volume").locale("markerBlock").oldReg("markerBlock").model("marker_volume");
        registerTag("item.block.marker.path").reg("marker_path").locale("pathMarkerBlock").oldReg("pathMarkerBlock").model("marker_path");
        registerTag("item.block.spring").reg("spring").locale("spring").model("spring");
        registerTag("item.block.decorated").reg("decorated").locale("decorated").model("decorated/");
        registerTag("item.block.engine.bc").reg("engine").locale("engineBlock").model("engine/");
        // Blocks
        registerTag("block.spring").reg("spring").locale("spring");
        registerTag("block.decorated").reg("decorated").locale("decorated");
        registerTag("block.engine.bc").reg("engine").locale("engineBlock").oldReg("engineBlock");
        registerTag("block.engine.bc.wood").locale("engineBlockWood");
        registerTag("block.engine.bc.stone").locale("engineBlockStone");
        registerTag("block.engine.bc.iron").locale("engineBlockIron");
        registerTag("block.engine.bc.creative").locale("engineBlockCreative");
        registerTag("block.marker.volume").reg("marker_volume").locale("markerBlock").oldReg("markerBlock").model("marker_volume");
        registerTag("block.marker.path").reg("marker_path").locale("pathMarkerBlock").oldReg("pathMarkerBlock").model("marker_path");
        // Tiles
        registerTag("tile.marker.volume").reg("marker.volume").oldReg("buildcraft.builders.Marker", "Marker");
        registerTag("tile.marker.path").reg("marker.path");
        registerTag("tile.engine.wood").reg("engine.wood");

        endBatch(TagManager.prependTags("buildcraftcore:", EnumTagType.REGISTRY_NAME, EnumTagType.MODEL_LOCATION).andThen(TagManager.setTab("buildcraft.main")));
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
