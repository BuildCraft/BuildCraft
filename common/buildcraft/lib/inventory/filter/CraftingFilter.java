/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.lib.inventory.filter;

import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

import buildcraft.api.core.IStackFilter;

import buildcraft.lib.misc.StackUtil;

import javax.annotation.Nonnull;

/** Returns true if the stack matches any one one of the filter stacks. Checks the OreDictionary and wildcards. */
public class CraftingFilter implements IStackFilter {

    private final NonNullList<ItemStack> stacks;

    public CraftingFilter(ItemStack... stacks) {
        this.stacks = StackUtil.listOf(stacks);
    }

    @Override
    public boolean matches(@Nonnull ItemStack stack) {
        if (stacks.size() == 0 || !hasFilter()) {
            return true;
        }
        for (ItemStack stack1 : stacks) {
            if (StackUtil.isCraftingEquivalent(stack1, stack, true)) {
                return true;
            }
        }
        return false;
    }

    public NonNullList<ItemStack> getStacks() {
        return stacks;
    }

    public boolean hasFilter() {
        for (ItemStack filter : stacks) {
            if (filter != null) {
                return true;
            }
        }
        return false;
    }
}
