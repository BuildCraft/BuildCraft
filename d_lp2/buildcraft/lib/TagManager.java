package buildcraft.lib;

import java.util.*;

import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;

/** Stores several types of "tag" (strings) for BuildCraft. A central place for all of them to init in. Refer to the
 * "static" block for all of the tag ID's */
public class TagManager {
    private static final Map<String, TagEntry> idsToEntry = new HashMap<>();

    public static Item getItem(String id) {
        String regTag = getTag(id, EnumTagType.REGISTRY_NAME);
        ResourceLocation loc = new ResourceLocation(regTag);
        if (Item.itemRegistry.containsKey(loc)) {
            return Item.itemRegistry.getObject(loc);
        } else {
            return null;
        }
    }

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

    public enum EnumTagType {
        UNLOCALIZED_NAME,
        OREDICT_NAME,
        REGISTRY_NAME,
        CREATIVE_TAB,
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
        return entry;
    }

    static {
        // BC Core Items
        registerTag("item.wrench").reg("buildcraftcore:wrench").locale("wrenchItem").oldReg("wrenchItem").tab("buildcraft.main");
        registerTag("item.gear.wood").reg("buildcraftcore:gear_wood").locale("woodenGearItem").oreDict("gearWood").oldReg("woodenGearItem").tab("buildcraft.main");
        registerTag("item.gear.stone").reg("buildcraftcore:gear_stone").locale("stoneGearItem").oreDict("gearStone").oldReg("stoneGearItem").tab("buildcraft.main");
        registerTag("item.gear.iron").reg("buildcraftcore:gear_iron").locale("ironGearItem").oreDict("gearIron").oldReg("ironGearItem").tab("buildcraft.main");
        registerTag("item.gear.gold").reg("buildcraftcore:gear_gold").locale("goldGearItem").oreDict("gearGold").oldReg("goldGearItem").tab("buildcraft.main");
        registerTag("item.gear.diamond").reg("buildcraftcore:gear_diamond").locale("diamondGearItem").oreDict("gearDiamond").oldReg("diamondGearItem").tab("buildcraft.main");
        registerTag("item.list").reg("buildcraftcore:list").locale("listItem").oldReg("listItem").tab("buildcraft.main");
        registerTag("item.map.location").reg("buildcraftcore:map_location").locale("mapLocationItem").oldReg("mapLocationItem").tab("buildcraft.main");
        // BC Core Blocks
        // BC Builders Items
        // BC Builders Blocks
        // BC Energy Items
        // BC Energy Blocks
        registerTag("block.engine.stone").reg("buildcraftenergy:engine_stone").locale("");
    }
}
