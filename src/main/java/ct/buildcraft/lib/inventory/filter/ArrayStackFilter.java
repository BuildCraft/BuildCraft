/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.lib.inventory.filter;

import java.util.Arrays;

import javax.annotation.Nonnull;

import ct.buildcraft.api.core.IStackFilter;

import ct.buildcraft.api.recipes.StackDefinition;
import ct.buildcraft.lib.misc.StackUtil;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

/** Returns true if the stack matches any one one of the filter stacks. */
public class ArrayStackFilter implements IStackFilter {

    protected NonNullList<ItemStack> stacks;

    public ArrayStackFilter(ItemStack... stacks) {
        this.stacks = StackUtil.listOf(stacks);
    }

    @Override
    public boolean matches(@Nonnull ItemStack stack) {
        if (stacks.size() == 0 || !hasFilter()) {
            return true;
        }
        for (ItemStack s : stacks) {
            if (ItemStack.isSame(s, stack)) {
                return true;
            }
        }
        return false;
    }

    public boolean matches(IStackFilter filter2) {
        for (ItemStack s : stacks) {
            if (filter2.matches(s)) {
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
            if (!filter.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public NonNullList<ItemStack> getExamples() {
        return stacks;
    }

    public static StackDefinition definition(int count, ItemStack... stacks) {
        return new StackDefinition(new ArrayStackFilter(stacks), count);
    }

    public static StackDefinition definition(ItemStack... stacks) {
        return definition(1, stacks);
    }

    public static StackDefinition definition(int count, Block... blocks) {
        return definition(count, Arrays.stream(blocks).map(ItemStack::new).toArray(ItemStack[]::new));
    }

    public static StackDefinition definition(Block... blocks) {
        return definition(1, blocks);
    }

    public static StackDefinition definition(int count, Item... items) {
        return definition(count, Arrays.stream(items).map(ItemStack::new).toArray(ItemStack[]::new));
    }

    public static StackDefinition definition(Item... items) {
        return definition(1, items);
    }
}
