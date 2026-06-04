/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.inventory.filter;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import buildcraft.lib.compat.forge_stubs.OreDictionaryStub;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;

// STUB(R.Chen): OreDictionaryStub removed — TODO(R.Chen): implement via Tags

import buildcraft.api.core.IStackFilter;
import buildcraft.api.recipes.StackDefinition;

import buildcraft.lib.misc.StackUtil;

/** Returns true if the stack matches any one one of the filter stacks. */
public class OreStackFilter implements IStackFilter {

    private final String[] ores;

    public OreStackFilter(String... iOres) {
        ores = iOres;
    }

    @Override
    public boolean matches(@Nonnull ItemStack stack) {
        int[] ids = OreDictionaryStub.getOreIDs(stack);

        if (ids.length == 0) {
            return false;
        }

        for (String ore : ores) {
            int expected = OreDictionaryStub.getOreID(ore);

            for (int id : ids) {
                if (id == expected) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public DefaultedList<ItemStack> getExamples() {
        return Arrays.stream(ores).map(OreDictionaryStub::getOres).flatMap(Collection::stream).distinct().collect(StackUtil.nonNullListCollector());
    }

    public static StackDefinition definition(int count, String... ores) {
        return new StackDefinition(new OreStackFilter(ores), count);
    }

    public static StackDefinition definition( String... ores) {
        return definition(1, ores);
    }
}
