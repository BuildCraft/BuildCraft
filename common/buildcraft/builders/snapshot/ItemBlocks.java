/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.snapshot;

import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.tags.ItemTags;
import net.minecraftforge.common.Tags;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.StreamSupport;

public class ItemBlocks {
    private static final List<Item> LIST = new ArrayList<>();

    static {
//        StreamSupport.stream(Item.REGISTRY.spliterator(), false)
        StreamSupport.stream(ForgeRegistries.ITEMS.spliterator(), false)
//                .filter(item -> item instanceof BlockItem || item instanceof BlockItemSpecial)
                .filter(item -> item instanceof BlockItem)
                .forEach(ItemBlocks::add);
//        add(
////                Items.BED,
////                Items.OAK_DOOR,
////                Items.SPRUCE_DOOR,
////                Items.BIRCH_DOOR,
////                Items.JUNGLE_DOOR,
////                Items.ACACIA_DOOR,
////                Items.DARK_OAK_DOOR,
////                Items.IRON_DOOR
////                Items.SKULL
////                Items.SIGN
//        );
        ItemTags.BEDS.getValues().forEach(ItemBlocks::add);
        ItemTags.DOORS.getValues().forEach(ItemBlocks::add);
        Tags.Items.HEADS.getValues().forEach(ItemBlocks::add);
        ItemTags.SIGNS.getValues().forEach(ItemBlocks::add);
    }

    public static void add(Item... items) {
        LIST.addAll(Arrays.asList(items));
    }

    public static List<Item> getList() {
        return Collections.unmodifiableList(LIST);
    }
}
