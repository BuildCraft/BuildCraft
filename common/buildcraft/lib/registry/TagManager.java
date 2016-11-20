/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.lib.registry;

import java.util.*;
import java.util.function.Consumer;

import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.fml.common.registry.ForgeRegistries;

/** Stores several types of "tag" (strings) for BuildCraft. A central place for all of them to init in. Refer to the
 * "static" block for all of the tag ID's
 * 
 * You are free to add your own tags (say for addons) but it is recommended that you include your addon name somewhere
 * near the start - we don't want name clashes between addons or an addon and BC itself. If you want more types of tags
 * keys then just make an issue for it, and it will probably be added. */
public class TagManager {
    private static final Map<String, TagEntry> idsToEntry = new HashMap<>();

    public static Item getItem(String id) {
        String regTag = getTag(id, EnumTagType.REGISTRY_NAME);
        ResourceLocation loc = new ResourceLocation(regTag);
        if (ForgeRegistries.ITEMS.containsKey(loc)) {
            return ForgeRegistries.ITEMS.getValue(loc);
        } else {
            return null;
        }
    }

    // getBlock?

    public static String getTag(String id, EnumTagType type) {
        if (idsToEntry.containsKey(id)) {
            TagEntry entry = idsToEntry.get(id);
            return entry.getSingleTag(type);
        } else {
            throw new IllegalArgumentException("Unknown id " + id);
        }
    }

    public static boolean hasTag(String id, EnumTagType type) {
        if (idsToEntry.containsKey(id)) {
            TagEntry entry = idsToEntry.get(id);
            return entry.tags.containsKey(type);
        } else {
            throw new IllegalArgumentException("Unknown id " + id);
        }
    }

    public static String[] getMultiTag(String id, EnumTagTypeMulti type) {
        if (idsToEntry.containsKey(id)) {
            TagEntry entry = idsToEntry.get(id);
            return entry.getMultiTag(type);
        } else {
            throw new IllegalArgumentException("Unknown id " + id);
        }
    }

    // hasMultiTag?

    public enum EnumTagType {
        UNLOCALIZED_NAME,
        OREDICT_NAME,
        REGISTRY_NAME,
        CREATIVE_TAB,
        MODEL_LOCATION,
    }

    public enum EnumTagTypeMulti {
        OLD_REGISTRY_NAME,
    }

    public static class TagEntry {
        /** The actual ID */
        public final String id;
        private final Map<EnumTagType, String> tags = new EnumMap<>(EnumTagType.class);
        private final Map<EnumTagTypeMulti, List<String>> multiTags = new EnumMap<>(EnumTagTypeMulti.class);

        public TagEntry(String id) {
            this.id = id;

        }

        public String getSingleTag(EnumTagType type) {
            if (!tags.containsKey(type)) throw new IllegalArgumentException("Unknown tag type " + type + " for the entry " + id);
            return tags.get(type);
        }

        public boolean hasSingleTag(EnumTagType type) {
            return tags.containsKey(type);
        }

        public String[] getMultiTag(EnumTagTypeMulti type) {
            List<String> ts = multiTags.get(type);
            if (ts == null) return new String[0];
            return ts.toArray(new String[ts.size()]);
        }

        public TagEntry setSingleTag(EnumTagType type, String tag) {
            tags.put(type, tag);
            return this;
        }

        public TagEntry reg(String name) {
            return setSingleTag(EnumTagType.REGISTRY_NAME, name);
        }

        public TagEntry locale(String name) {
            return setSingleTag(EnumTagType.UNLOCALIZED_NAME, name);
        }

        public TagEntry oreDict(String name) {
            return setSingleTag(EnumTagType.OREDICT_NAME, name);
        }

        public TagEntry tab(String creativeTab) {
            return setSingleTag(EnumTagType.CREATIVE_TAB, creativeTab);
        }

        public TagEntry model(String modelLocation) {
            return setSingleTag(EnumTagType.MODEL_LOCATION, modelLocation);
        }

        public TagEntry addMultiTag(EnumTagTypeMulti type, String... tags) {
            if (!this.tags.containsKey(type)) {
                this.multiTags.put(type, new LinkedList<>());
            }
            for (String tag : tags) {
                multiTags.get(type).add(tag);
            }
            return this;
        }

        public TagEntry oldReg(String... tags) {
            return addMultiTag(EnumTagTypeMulti.OLD_REGISTRY_NAME, tags);
        }
    }

    public static TagEntry registerTag(String id) {
        TagEntry entry = new TagEntry(id);
        idsToEntry.put(id, entry);
        for (List<TagEntry> list : batchTasks) {
            list.add(entry);
        }
        return entry;
    }

    // #########################
    //
    // Batching repetitive tags
    //
    // #########################

    private static final Deque<List<TagEntry>> batchTasks = new ArrayDeque<>();

    public static void startBatch() {
        batchTasks.push(new ArrayList<>());
    }

    public static void endBatch(Consumer<TagEntry> consumer) {
        batchTasks.pop().forEach(consumer);
    }

    public static Consumer<TagEntry> prependTag(EnumTagType type, String prefix) {
        return tag -> {
            if (tag.hasSingleTag(type)) {
                tag.setSingleTag(type, prefix + tag.getSingleTag(type));
            }
        };
    }

    public static Consumer<TagEntry> prependTags(String prefix, EnumTagType... tags) {
        Consumer<TagEntry> consumer = tag -> {};
        for (EnumTagType type : tags) {
            consumer = consumer.andThen(prependTag(type, prefix));
        }
        return consumer;
    }

    public static Consumer<TagEntry> set(EnumTagType type, String value) {
        return tag -> tag.setSingleTag(type, value);
    }

    public static Consumer<TagEntry> setTab(String creativeTab) {
        return tag -> {
            if (tag.hasSingleTag(EnumTagType.REGISTRY_NAME) && !tag.hasSingleTag(EnumTagType.CREATIVE_TAB)) {
                tag.tab(creativeTab);
            }
        };
    }

    // #########################
    //
    // BuildCraft definitions
    //
    // #########################

    static {
        startBatch();// BC

        startBatch(); // lib
        registerTag("item.guide").reg("guide").locale("guide").model("guide").tab("vanilla.misc");
        registerTag("item.debugger").reg("debugger").locale("debugger").model("debugger").tab("vanilla.misc");
        endBatch(prependTags("lib:", EnumTagType.REGISTRY_NAME, EnumTagType.MODEL_LOCATION));

        startBatch();// core
        // BC Core Items
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
        // BC Core ItemBlocks
        registerTag("item.block.marker.volume").reg("marker_volume").locale("markerBlock").oldReg("markerBlock").model("marker_volume");
        registerTag("item.block.marker.path").reg("marker_path").locale("pathMarkerBlock").oldReg("pathMarkerBlock").model("marker_path");
        registerTag("item.block.spring").reg("spring").locale("spring").model("spring");
        registerTag("item.block.decorated").reg("decorated").locale("decorated").model("decorated/");
        registerTag("item.block.engine.bc").reg("engine").locale("engineBlock").model("engine/");
        // BC Core Blocks
        registerTag("block.spring").reg("spring").locale("spring");
        registerTag("block.decorated").reg("decorated").locale("decorated");
        registerTag("block.engine.bc").reg("engine").locale("engineBlock").oldReg("engineBlock");
        registerTag("block.engine.bc.wood").locale("engineBlockWood");
        registerTag("block.engine.bc.stone").locale("engineBlockStone");
        registerTag("block.engine.bc.iron").locale("engineBlockIron");
        registerTag("block.engine.bc.creative").locale("engineBlockCreative");
        registerTag("block.marker.volume").reg("marker_volume").locale("markerBlock").oldReg("markerBlock").model("marker_volume");
        registerTag("block.marker.path").reg("marker_path").locale("pathMarkerBlock").oldReg("pathMarkerBlock").model("marker_path");
        // BC Core Tiles
        registerTag("tile.marker.volume").reg("marker.volume").oldReg("buildcraft.builders.Marker", "Marker");
        registerTag("tile.marker.path").reg("marker.path");
        registerTag("tile.engine.wood").reg("engine.wood");

        endBatch(prependTags("core:", EnumTagType.REGISTRY_NAME, EnumTagType.MODEL_LOCATION));

        startBatch();// builders
        // BC Builders Items
        registerTag("item.schematic.single").reg("schematic_single").locale("schematicSingle").model("schematic_single/");
        registerTag("item.blueprint").reg("blueprint").locale("blueprintItem").model("blueprint/");
        // BC Builders Item Blocks
        registerTag("item.block.architect").reg("architect").locale("architectBlock").model("architect");
        registerTag("item.block.builder").reg("builder").locale("builderBlock").model("builder");
        registerTag("item.block.filler").reg("filler").locale("fillerBlock").model("filler");
        registerTag("item.block.library").reg("library").locale("libraryBlock").model("library");
        registerTag("item.block.frame").reg("frame").locale("frameBlock").model("frame");
        registerTag("item.block.quarry").reg("quarry").locale("quarryBlock").model("quarry");
        // BC Builders Blocks
        registerTag("block.architect").reg("architect").locale("architectBlock").model("architect");
        registerTag("block.builder").reg("builder").locale("builderBlock").model("builder");
        registerTag("block.filler").reg("filler").locale("fillerBlock").model("filler");
        registerTag("block.library").reg("library").locale("libraryBlock").model("library");
        registerTag("block.frame").reg("frame").locale("frameBlock").model("frame");
        registerTag("block.quarry").reg("quarry").locale("quarryBlock").model("quarry");
        // BC Builders Tiles
        registerTag("tile.architect").reg("architect");
        registerTag("tile.library").reg("library");
        registerTag("tile.builder").reg("builder");
        registerTag("tile.filler").reg("filler");
        registerTag("tile.quarry").reg("quarry");

        endBatch(prependTags("builders:", EnumTagType.REGISTRY_NAME, EnumTagType.MODEL_LOCATION));

        startBatch();// energy
        registerTag("item.glob.oil").reg("glob_oil").locale("globOil").model("glob_oil");
        // BC Energy Items
        // BC Energy Item Blocks
        // BC Energy Blocks
        // BC Energy Tiles
        registerTag("tile.engine.stone").reg("engine.stone");

        endBatch(prependTags("energy:", EnumTagType.REGISTRY_NAME, EnumTagType.MODEL_LOCATION));

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
        // BC Factory Blocks
        registerTag("block.plastic").reg("plastic").locale("plasticBlock").model("plastic");
        registerTag("block.autoworkbench.item").reg("autoworkbench_item").oldReg("autoWorkbenchBlock").locale("autoWorkbenchBlock").model("autoworkbench_item");
        registerTag("block.mining_well").reg("mining_well").oldReg("miningWellBlock").locale("miningWellBlock").model("mining_well");
        registerTag("block.pump").reg("pump").oldReg("pumpBlock").locale("pumpBlock").model("pump");
        registerTag("block.flood_gate").reg("flood_gate").oldReg("floodGateBlock").locale("floodGateBlock").model("flood_gate");
        registerTag("block.tank").reg("tank").oldReg("tankBlock").locale("tankBlock").model("tank");
        registerTag("block.chute").reg("chute").oldReg("chuteBlock").locale("chuteBlock").model("chute");
        registerTag("block.water_gel").reg("water_gel").locale("waterGel").model("water_gel");
        // BC Factory Tiles
        registerTag("tile.autoworkbench.item").reg("autoworkbench_item");
        registerTag("tile.mining_well").reg("mining_well");
        registerTag("tile.pump").reg("pump");
        registerTag("tile.flood_gate").reg("flood_gate");
        registerTag("tile.tank").reg("tank");
        registerTag("tile.chute").reg("chute");

        endBatch(prependTags("factory:", EnumTagType.REGISTRY_NAME, EnumTagType.MODEL_LOCATION));

        startBatch();// transport
        // BC Transport Items
        registerTag("item.waterproof").reg("waterproof").locale("pipeWaterproof").oldReg("pipeWaterproof").model("waterproof");
        registerTag("item.plug.blocker").reg("plug_blocker").locale("PipePlug").model("plug_blocker");
        registerTag("item.plug.gate").reg("plug_gate").locale("gate").model("pluggable/gate").tab("buildcraft.gate");
        registerTag("item.plug.pulsar").reg("plug_pulsar").locale("pulsar").model("plug_pulsar");
        registerTag("item.wire").reg("wire").locale("wire").model("wire/");
        // BC Transport Pipes
        startBatch();// Pipes
        registerTag("item.pipe.buildcrafttransport.structure").reg("pipe_structure").locale("PipeStructureCobblestone");
        registerTag("item.pipe.buildcrafttransport.wood_item").reg("pipe_wood_item").locale("PipeItemsWood");
        registerTag("item.pipe.buildcrafttransport.wood_fluid").reg("pipe_wood_fluid").locale("PipeFluidsWood");
        registerTag("item.pipe.buildcrafttransport.wood_power").reg("pipe_wood_power").locale("PipePowerWood");
        registerTag("item.pipe.buildcrafttransport.stone_item").reg("pipe_stone_item").locale("PipeItemsStone");
        registerTag("item.pipe.buildcrafttransport.stone_fluid").reg("pipe_stone_fluid").locale("PipeFluidsStone");
        registerTag("item.pipe.buildcrafttransport.stone_power").reg("pipe_stone_power").locale("PipePowerStone");
        registerTag("item.pipe.buildcrafttransport.cobblestone_item").reg("pipe_cobble_item").locale("PipeItemsCobblestone");
        registerTag("item.pipe.buildcrafttransport.cobblestone_fluid").reg("pipe_cobble_fluid").locale("PipeFluidsCobblestone");
        registerTag("item.pipe.buildcrafttransport.cobblestone_power").reg("pipe_cobble_power").locale("PipePowerCobblestone");
        registerTag("item.pipe.buildcrafttransport.quartz_item").reg("pipe_quartz_item").locale("PipeItemsQuartz");
        registerTag("item.pipe.buildcrafttransport.quartz_fluid").reg("pipe_quartz_fluid").locale("PipeFluidsQuartz");
        registerTag("item.pipe.buildcrafttransport.quartz_power").reg("pipe_quartz_power").locale("PipePowerQuartz");
        registerTag("item.pipe.buildcrafttransport.gold_item").reg("pipe_gold_item").locale("PipeItemsGold");
        registerTag("item.pipe.buildcrafttransport.gold_fluid").reg("pipe_gold_fluid").locale("PipeFluidsGold");
        registerTag("item.pipe.buildcrafttransport.gold_power").reg("pipe_gold_power").locale("PipePowerGold");
        registerTag("item.pipe.buildcrafttransport.sandstone_item").reg("pipe_sandstone_item").locale("PipeItemsSandstone");
        registerTag("item.pipe.buildcrafttransport.sandstone_fluid").reg("pipe_sandstone_fluid").locale("PipeFluidsSandstone");
        registerTag("item.pipe.buildcrafttransport.sandstone_power").reg("pipe_sandstone_power").locale("PipePowerSandstone");
        registerTag("item.pipe.buildcrafttransport.iron_item").reg("pipe_iron_item").locale("PipeItemsIron");
        registerTag("item.pipe.buildcrafttransport.iron_fluid").reg("pipe_iron_fluid").locale("PipeFluidsIron");
        registerTag("item.pipe.buildcrafttransport.iron_power").reg("pipe_iron_power").locale("PipePowerIron");
        registerTag("item.pipe.buildcrafttransport.diamond_item").reg("pipe_diamond_item").locale("PipeItemsDiamond");
        registerTag("item.pipe.buildcrafttransport.diamond_fluid").reg("pipe_diamond_fluid").locale("PipeFluidsDiamond");
        registerTag("item.pipe.buildcrafttransport.diamond_power").reg("pipe_diamond_power").locale("PipePowerDiamond");
        registerTag("item.pipe.buildcrafttransport.diamond_wood_item").reg("pipe_diamond_wood_item").locale("PipeItemsEmerald");
        registerTag("item.pipe.buildcrafttransport.diamond_wood_fluid").reg("pipe_diamond_wood_fluid").locale("PipeFluidsEmerald");
        registerTag("item.pipe.buildcrafttransport.diamond_wood_power").reg("pipe_diamond_wood_power").locale("PipePowerEmerald");
        registerTag("item.pipe.buildcrafttransport.clay_item").reg("pipe_clay_item").locale("PipeItemsClay");
        registerTag("item.pipe.buildcrafttransport.clay_fluid").reg("pipe_clay_fluid").locale("PipeFluidsClay");
        registerTag("item.pipe.buildcrafttransport.void_item").reg("pipe_void_item").locale("PipeItemsVoid");
        registerTag("item.pipe.buildcrafttransport.void_fluid").reg("pipe_void_fluid").locale("PipeFluidsVoid");
        registerTag("item.pipe.buildcrafttransport.obsidian_item").reg("pipe_obsidian_item").locale("PipeItemsObsidian");
        registerTag("item.pipe.buildcrafttransport.obsidian_fluid").reg("pipe_obsidian_fluid").locale("PipeFluidsObsidian");
        registerTag("item.pipe.buildcrafttransport.lapis_item").reg("pipe_lapis_fluid").locale("PipeItemsLapis");
        registerTag("item.pipe.buildcrafttransport.daizuli_item").reg("pipe_daizuli_fluid").locale("PipeItemsDaizuli");
        endBatch(setTab("buildcraft.pipe"));
        // BC Transport Item Blocks
        registerTag("item.block.filtered_buffer").reg("filtered_buffer").locale("filteredBufferBlock").model("filtered_buffer");
        // BC Transport Blocks
        registerTag("block.filtered_buffer").reg("filtered_buffer").oldReg("filteredBufferBlock").locale("filteredBufferBlock").model("filtered_buffer");
        registerTag("block.pipe_holder").reg("pipe_holder").locale("pipeHolder");
        // BC Transport Tiles
        registerTag("tile.filtered_buffer").reg("filtered_buffer");
        registerTag("tile.pipe_holder").reg("pipe_holder");

        endBatch(prependTags("transport:", EnumTagType.REGISTRY_NAME, EnumTagType.MODEL_LOCATION));

        startBatch();// robotics
        // BC Robotics Items
        // BC Robotics Item Blocks
        registerTag("item.block.zone_planner").reg("zone_planner").locale("zonePlannerBlock").model("zone_planner");
        // BC Robotics Blocks
        registerTag("block.zone_planner").reg("zone_planner").oldReg("zonePlannerBlock").locale("zonePlannerBlock").model("zone_planner");
        // BC Robotics Tiles
        registerTag("tile.zone_planner").reg("zone_planner");

        endBatch(prependTags("robotics:", EnumTagType.REGISTRY_NAME, EnumTagType.MODEL_LOCATION));

        startBatch();// silicon
        // BC Silicon Items
        registerTag("item.redstone_chipset").reg("redstone_chipset").locale("redstone_chipset").model("redstone_chipset/");
        // BC Silicon Item Blocks
        registerTag("item.block.laser").reg("laser").locale("laserBlock").model("laser");
        registerTag("item.block.assembly_table").reg("assembly_table").locale("assemblyTableBlock").model("assembly_table");
        registerTag("item.block.advanced_crafting_table").reg("advanced_crafting_table").locale("advancedCraftingTableBlock").model("advanced_crafting_table");
        registerTag("item.block.integration_table").reg("integration_table").locale("integrationTableBlock").model("integration_table");
        registerTag("item.block.charging_table").reg("charging_table").locale("chargingTableBlock").model("charging_table");
        registerTag("item.block.programming_table").reg("programming_table").locale("programmingTableBlock").model("programming_table");
        // BC Silicon Blocks
        registerTag("block.laser").reg("laser").oldReg("laserBlock").locale("laserBlock").model("laser");
        registerTag("block.assembly_table").reg("assembly_table").oldReg("assemblyTableBlock").locale("assemblyTableBlock").model("assembly_table");
        registerTag("block.advanced_crafting_table").reg("advanced_crafting_table").oldReg("advancedCraftingTableBlock").locale("advancedCraftingTableBlock").model("advanced_crafting_table");
        registerTag("block.integration_table").reg("integration_table").oldReg("integrationTableBlock").locale("integrationTableBlock").model("integration_table");
        registerTag("block.charging_table").reg("charging_table").oldReg("chargingTableBlock").locale("chargingTableBlock").model("charging_table");
        registerTag("block.programming_table").reg("programming_table").oldReg("programmingTableBlock").locale("programmingTableBlock").model("programming_table");
        // BC Silicon Tiles
        registerTag("tile.laser").reg("laser");
        registerTag("tile.assembly_table").reg("assembly_table");
        registerTag("tile.advanced_crafting_table").reg("advanced_crafting_table");
        registerTag("tile.integration_table").reg("integration_table");
        registerTag("tile.charging_table").reg("charging_table");
        registerTag("tile.programming_table").reg("programming_table");

        endBatch(prependTags("silicon:", EnumTagType.REGISTRY_NAME, EnumTagType.MODEL_LOCATION));

        endBatch(prependTags("buildcraft", EnumTagType.REGISTRY_NAME, EnumTagType.MODEL_LOCATION).andThen(setTab("buildcraft.main")));
    }
}
