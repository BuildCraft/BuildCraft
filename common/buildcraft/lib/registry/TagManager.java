/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.lib.registry;


import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/** Stores several types of "tag" (strings) for BuildCraft. A central place for all of them to init in. Refer to the
 * "static" block for all of the tag ID's
 * <p>
 * You are free to add your own tags (say for addons) but it is recommended that you include your addon name somewhere
 * near the start - we don't want name clashes between addons or an addon and BC itself. If you want more types of tags
 * keys then just make an issue for it, and it will probably be added. */
public class TagManager {
    // Calen: thread safety
//    private static final Map<String, TagEntry> idsToEntry = new HashMap<>();
    private static final Map<String, TagEntry> idsToEntry = new ConcurrentHashMap<>();

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
        // Calen: not still used in 1.18.2
//        MODEL_LOCATION,
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
            if (!tags.containsKey(type)) {
                throw new IllegalArgumentException("Unknown tag type " + type + " for the entry " + id);
            }
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

        // Calen: not still used in 1.18.2
//        public TagEntry model(String modelLocation) {
//            return setSingleTag(EnumTagType.MODEL_LOCATION, modelLocation);
//        }

        public TagEntry addMultiTag(EnumTagTypeMulti type, String... tags) {
            if (!this.multiTags.containsKey(type)) {
                this.multiTags.put(type, new LinkedList<>());
            }
            for (String tag : tags) {
                multiTags.get(type).add(tag);
            }
            return this;
        }

//        public TagEntry oldReg(String... tags) {
//            return addMultiTag(EnumTagTypeMulti.OLD_REGISTRY_NAME, tags);
//        }
    }

    public static TagEntry getTag(String id) {
        return idsToEntry.get(id);
    }

    // public static TagEntry registerTag(String id)
    public TagEntry registerTag(String id) {
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

    // private static final Deque<List<TagEntry>> batchTasks = new ArrayDeque<>();
    private final Deque<List<TagEntry>> batchTasks = new ArrayDeque<>();

    // public static void startBatch()
    public void startBatch() {
        batchTasks.push(new ArrayList<>());
    }

    // public static void endBatch(Consumer<TagEntry> consumer)
    public void endBatch(Consumer<TagEntry> consumer) {
        batchTasks.pop().forEach(consumer);
    }

    public static Consumer<TagEntry> prependTag(EnumTagType type, String prefix) {
        return tag ->
        {
            if (tag == null) {
                throw new RuntimeException("[lib.tagmanager.prepend] Tag is null!");
            }
            if (tag.hasSingleTag(type)) {
                tag.setSingleTag(type, prefix + tag.getSingleTag(type));
            }
        };
    }

    public static Consumer<TagEntry> prependTags(String prefix, EnumTagType... tags) {
        Consumer<TagEntry> consumer = tag ->
        {
        };
        for (EnumTagType type : tags) {
            consumer = consumer.andThen(prependTag(type, prefix));
        }
        return consumer;
    }

    public static Consumer<TagEntry> set(EnumTagType type, String value) {
        return tag -> tag.setSingleTag(type, value);
    }

    public static Consumer<TagEntry> setTab(String creativeTab) {
        return tag ->
        {
            if (tag.hasSingleTag(EnumTagType.REGISTRY_NAME) && !tag.hasSingleTag(EnumTagType.CREATIVE_TAB)) {
                tag.tab(creativeTab);
            }
        };
    }
}
