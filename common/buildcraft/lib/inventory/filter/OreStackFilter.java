/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.inventory.filter;

import buildcraft.api.core.IStackFilter;
import buildcraft.api.recipes.StackDefinition;
import net.minecraft.core.NonNullList;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;

/** Returns true if the stack matches any one one of the filter stacks. */
public class OreStackFilter implements IStackFilter {

    // private final String[] ores;
    private final List<TagKey<Item>> ores = NonNullList.create();

    // public OreStackFilter(String... iOres)
    public OreStackFilter(TagKey<Item>... iOres) {
//        ores = iOres;
        ores.addAll(Arrays.asList(iOres));
    }

    @Override
    public boolean matches(@Nonnull ItemStack stack) {
//        int[] ids = OreDictionary.getOreIDs(stack);
//
//        if (ids.length == 0) {
//            return false;
//        }
//
//        for (String ore : ores) {
//            int expected = OreDictionary.getOreID(ore);
//
//            for (int id : ids) {
//                if (id == expected) {
//                    return true;
//                }
//            }
//        }
//
//        return false;

        return ores.stream().anyMatch(stack::is);
    }

    @Override
    public NonNullList<ItemStack> getExamples() {
//        return Arrays.stream(ores).map(OreDictionary::getOres).flatMap(Collection::stream).distinct().collect(StackUtil.nonNullListCollector());
        NonNullList<ItemStack> ret = NonNullList.create();
        ores.forEach(o -> ret.addAll(ForgeRegistries.ITEMS.tags().getTag(o).stream().map(ItemStack::new).toList()));
        return ret;
    }

    // public static StackDefinition definition(int count, String... ores)
    public static StackDefinition definition(int count, TagKey<Item>... ores) {
        return new StackDefinition(new OreStackFilter(ores), count);
    }

    // public static StackDefinition definition(String... ores)
    public static StackDefinition definition(TagKey<Item>... ores) {
        return definition(1, ores);
    }
}
