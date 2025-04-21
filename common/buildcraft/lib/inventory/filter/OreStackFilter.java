/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.inventory.filter;

import buildcraft.api.core.IStackFilter;
import buildcraft.api.recipes.StackDefinition;
import buildcraft.lib.misc.StackUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/** Returns true if the stack matches any one one of the filter stacks. */
public class OreStackFilter implements IStackFilter {

    // private final String[] ores;
    private final List<ResourceLocation> ores = NonNullList.create();

    // public OreStackFilter(String... iOres)
    public OreStackFilter(ResourceLocation... iOres) {
//        ores = iOres;
        ores.addAll(Arrays.asList(iOres));
    }

    @Override
    public boolean matches(@Nonnull ItemStack stack) {
//        int[] ids = OreDictionary.getOreIDs(stack);
        Set<ResourceLocation> ids = stack.getItem().getTags();

        if (ids.size() == 0) {
            return false;
        }

//        for (String ore : ores)
        for (ResourceLocation ore : ores) {
//            int expected = OreDictionary.getOreID(ore);

//            for (int id : ids)
            for (ResourceLocation id : ids) {
//                if (id == expected)
                if (id.equals(ore)) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public NonNullList<ItemStack> getExamples() {
//        return Arrays.stream(ores).map(OreDictionary::getOres).flatMap(Collection::stream).distinct().collect(StackUtil.nonNullListCollector());
        NonNullList<ItemStack> ret = NonNullList.create();
        ores.forEach(o -> ret.addAll(ItemTags.getAllTags().getTag(o).getValues().stream().map(ItemStack::new).collect(StackUtil.nonNullListCollector())));
        return ret;
    }

    // public static StackDefinition definition(int count, String... ores)
    public static StackDefinition definition(int count, ResourceLocation... ores) {
        return new StackDefinition(new OreStackFilter(ores), count);
    }

    // public static StackDefinition definition(String... ores)
    public static StackDefinition definition(ResourceLocation... ores) {
        return definition(1, ores);
    }
}
