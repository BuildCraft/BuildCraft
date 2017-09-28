/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.snapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.StreamSupport;

import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemBlockSpecial;

public class ItemBlocks {
    private static final List<Item> LIST = new ArrayList<>();

    static {
        StreamSupport.stream(Item.REGISTRY.spliterator(), false)
            .filter(item -> item instanceof ItemBlock || item instanceof ItemBlockSpecial)
            .forEach(ItemBlocks::add);
        add(
            Items.BED,
            Items.OAK_DOOR,
            Items.SPRUCE_DOOR,
            Items.BIRCH_DOOR,
            Items.JUNGLE_DOOR,
            Items.ACACIA_DOOR,
            Items.DARK_OAK_DOOR,
            Items.IRON_DOOR,
            Items.SKULL,
            Items.SIGN
        );
    }

    public static void add(Item... items) {
        LIST.addAll(Arrays.asList(items));
    }

    public static List<Item> getList() {
        return Collections.unmodifiableList(LIST);
    }
}
