/* Copyright (c) 2016 AlexIIL and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.lib;

import java.util.*;
import java.util.function.Consumer;

import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;

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
        if (Item.REGISTRY.containsKey(loc)) {
            return Item.REGISTRY.getObject(loc);
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
        endBatch(prependTags("lib:", EnumTagType.REGISTRY_NAME, EnumTagType.MODEL_LOCATION));

        startBatch();// core
        // BC Core Items
        registerTag("item.wrench").reg("wrench").locale("wrenchItem").oldReg("wrenchItem").model("wrench");
        registerTag("item.gear.wood").reg("gear_wood").locale("woodenGearItem").oreDict("gearWood").oldReg("woodenGearItem").model("gears/wood");
        registerTag("item.gear.stone").reg("gear_stone").locale("stoneGearItem").oreDict("gearStone").oldReg("stoneGearItem").model("gears/stone");
        registerTag("item.gear.iron").reg("gear_iron").locale("ironGearItem").oreDict("gearIron").oldReg("ironGearItem").model("gears/iron");
        registerTag("item.gear.gold").reg("gear_gold").locale("goldGearItem").oreDict("gearGold").oldReg("goldGearItem").model("gears/gold");
        registerTag("item.gear.diamond").reg("gear_diamond").locale("diamondGearItem").oreDict("gearDiamond").oldReg("diamondGearItem").model("gears/diamond");
        registerTag("item.list").reg("list").locale("list").oldReg("listItem").model("list_");
        registerTag("item.map_location").reg("map_location").locale("mapLocation").oldReg("mapLocation").model("map_location/");
        registerTag("item.paintbrush").reg("paintbrush").locale("paintbrush").model("paintbrush/");
        registerTag("item.marker_connector").reg("marker_connector").locale("markerConnector").model("marker_connector");
        // BC Core ItemBlocks
        registerTag("item.block.marker.volume").reg("marker_volume").locale("markerBlock").oldReg("markerBlock").model("marker_volume");
        registerTag("item.block.marker.path").reg("marker_path").locale("pathMarkerBlock").oldReg("pathMarkerBlock").model("marker_path");
        registerTag("item.block.decorated").reg("decorated").locale("decorated").model("decorated/alpha/");
        // BC Core Blocks
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

        endBatch(prependTags("core:", EnumTagType.REGISTRY_NAME, EnumTagType.MODEL_LOCATION));

        startBatch();// builders
        // BC Builders Items
        registerTag("item.schematic.single").reg("schematic_single").locale("schematicSingle").model("schematic_single/");
        registerTag("item.blueprint").reg("blueprint").locale("blueprintItem").model("blueprint/");
        // BC Builders Item Blocks
        registerTag("item.block.architect").reg("architect").locale("architectBlock").model("architect");
        registerTag("item.block.builder").reg("builder").locale("builderBlock").model("builder");
        registerTag("item.block.library").reg("library").locale("libraryBlock").model("library");
        // BC Builders Blocks
        registerTag("block.architect").reg("architect").locale("architectBlock").model("architect");
        registerTag("block.builder").reg("builder").locale("builderBlock").model("builder");
        registerTag("block.library").reg("library").locale("libraryBlock").model("library");
        // BC Builders Tiles
        registerTag("tile.architect").reg("architect");
        registerTag("tile.library").reg("library");

        endBatch(prependTags("builders:", EnumTagType.REGISTRY_NAME, EnumTagType.MODEL_LOCATION));

        startBatch();// energy
        registerTag("item.glob.oil").reg("glob_oil").locale("globOil").model("glob_oil");
        // BC Energy Items
        // BC Energy Item Blocks
        // BC Energy Blocks
        // BC Energy Tiles

        endBatch(prependTags("energy:", EnumTagType.REGISTRY_NAME, EnumTagType.MODEL_LOCATION));

        startBatch();// factory
        registerTag("item.plastic.sheet").reg("plastic_sheet").locale("plasticSheet").oldReg("plasticSheet").model("plastic_sheet");
        // BC Factory Item Blocks
        registerTag("item.block.plastic").reg("plastic_block").locale("plasticBlock").model("plastic_block/");
        registerTag("item.block.autoworkbench.item").reg("autoworkbench_item").locale("autoWorkbenchBlock").model("autoworkbench_item");
        registerTag("item.block.mining_well").reg("mining_well").locale("miningWellBlock").model("mining_well");
        registerTag("item.block.pump").reg("pump").locale("pumpBlock").model("pump");
        registerTag("item.block.plain_pipe").reg("plain_pipe").locale("plainPipeBlock").model("plain_pipe");
        // BC Factory Blocks
        registerTag("block.plastic").reg("plastic").locale("plasticBlock").model("plastic");
        registerTag("block.autoworkbench.item").reg("autoworkbench_item").oldReg("autoWorkbenchBlock").locale("autoWorkbenchBlock").model("autoworkbench_item");
        registerTag("block.mining_well").reg("mining_well").oldReg("miningWellBlock").locale("miningWellBlock").model("mining_well");
        registerTag("block.pump").reg("pump").oldReg("pumpBlock").locale("pumpBlock").model("pump");
        registerTag("block.plain_pipe").reg("plain_pipe").oldReg("plainPipeBlock").locale("plainPipeBlock").model("plain_pipe");
        // BC Factory Tiles
        registerTag("tile.autoworkbench.item").reg("autoworkbench_item");
        registerTag("tile.mining_well").reg("mining_well");
        registerTag("tile.pump").reg("pump");

        endBatch(prependTags("factory:", EnumTagType.REGISTRY_NAME, EnumTagType.MODEL_LOCATION));

        startBatch();// transport
        // BC Transport Items
        registerTag("item.waterproof").reg("waterproof").locale("pipeWaterproof").oldReg("pipeWaterproof").model("waterproof");
        // BC Transport Blocks

        endBatch(prependTags("transport:", EnumTagType.REGISTRY_NAME, EnumTagType.MODEL_LOCATION));

        endBatch(prependTags("buildcraft", EnumTagType.REGISTRY_NAME, EnumTagType.MODEL_LOCATION).andThen(setTab("buildcraft.main")));
    }
}
