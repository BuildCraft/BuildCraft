/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.inventory.filter;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;

import net.minecraftforge.oredict.OreDictionary;

import buildcraft.api.core.IStackFilter;
import buildcraft.api.recipes.StackDefinition;


/** Returns true if the stack matches any one one of the filter stacks. */
public class OreStackFilter implements IStackFilter {

    private final String[] ores;

    public OreStackFilter(String... iOres) {
        ores = iOres;
    }

    @Override
    public boolean matches(@Nonnull ItemStack stack) {
        int[] ids = OreDictionary.getOreIDs(stack);

        if (ids.length == 0) {
            return false;
        }

        for (String ore : ores) {
            int expected = OreDictionary.getOreID(ore);

            for (int id : ids) {
                if (id == expected) {
                    return true;
                }
            }
        }

        return false;
    }

    public static StackDefinition definition(int count, String... ores) {
        return new StackDefinition(new OreStackFilter(ores), count);
    }

    public static StackDefinition definition( String... ores) {
        return definition(1, ores);
    }
}
