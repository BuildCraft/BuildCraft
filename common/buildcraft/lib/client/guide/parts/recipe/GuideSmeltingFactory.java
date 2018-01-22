/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.guide.parts.recipe;

import java.util.Arrays;
import java.util.Map.Entry;

import javax.annotation.Nonnull;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;

import buildcraft.lib.client.guide.GuiGuide;
import buildcraft.lib.client.guide.parts.GuidePartFactory;

public class GuideSmeltingFactory implements GuidePartFactory {
    @Nonnull
    private final ItemStack input, output;
    private final int hash;

    public GuideSmeltingFactory(ItemStack input, ItemStack output) {
        this.input = input;
        this.output = output;
        this.hash = Arrays.hashCode(new int[] { input.serializeNBT().hashCode(), output.serializeNBT().hashCode() });
    }

    public static GuideSmeltingFactory create(ItemStack stack) {
        for (Entry<ItemStack, ItemStack> entry : FurnaceRecipes.instance().getSmeltingList().entrySet()) {
            if (ItemStack.areItemsEqual(stack, entry.getValue())) {
                return new GuideSmeltingFactory(entry.getKey(), stack);
            }
        }
        return null;
    }

    public static GuideSmeltingFactory create(Item output) {
        return create(new ItemStack(output));
    }

    @Override
    public GuideSmelting createNew(GuiGuide gui) {
        return new GuideSmelting(gui, input, output);
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null) return false;
        if (obj.getClass() != getClass()) return false;
        GuideSmeltingFactory other = (GuideSmeltingFactory) obj;
        // Shortcut out of this full itemstack comparison as its really expensive
        return hash == other.hash && ItemStack.areItemStacksEqual(input, other.input)//
                && ItemStack.areItemStacksEqual(output, other.output);

    }
}
