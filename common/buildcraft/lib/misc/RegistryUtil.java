/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class RegistryUtil {
    public static boolean isRegistered(Block block) {
        return ForgeRegistries.BLOCKS.containsValue(block);
    }

    public static boolean isRegistered(Item item) {
        return ForgeRegistries.ITEMS.containsValue(item);
    }
}
