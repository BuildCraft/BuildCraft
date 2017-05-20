/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.lib.inventory.filter;

import buildcraft.lib.misc.StackUtil;
import net.minecraft.item.ItemStack;

import net.minecraft.util.NonNullList;
import net.minecraftforge.oredict.OreDictionary;

import buildcraft.api.core.IStackFilter;
import buildcraft.api.recipes.StackDefinition;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collection;

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

    @Override
    public NonNullList<ItemStack> getExamples() {
        return Arrays.stream(ores).map(OreDictionary::getOres).flatMap(Collection::stream).distinct().collect(StackUtil.nonNullListCollector());
    }

    public static StackDefinition definition(int count, String... ores) {
        return new StackDefinition(new OreStackFilter(ores), count);
    }

    public static StackDefinition definition( String... ores) {
        return definition(1, ores);
    }
}
