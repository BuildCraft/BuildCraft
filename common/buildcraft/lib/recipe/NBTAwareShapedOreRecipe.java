/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.recipe;

import buildcraft.lib.misc.StackUtil;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;

import javax.annotation.Nonnull;
import java.util.Iterator;
import java.util.List;

public class NBTAwareShapedOreRecipe extends ShapedOreRecipe {

    public NBTAwareShapedOreRecipe(@Nonnull ItemStack result, Object... recipe) {
        super(result, recipe);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected boolean checkMatch(InventoryCrafting inv, int startX, int startY, boolean mirror) {
        for (int x = 0; x < MAX_CRAFT_GRID_WIDTH; x++) {
            for (int y = 0; y < MAX_CRAFT_GRID_HEIGHT; y++) {
                int subX = x - startX;
                int subY = y - startY;
                Object target = null;

                if (subX >= 0 && subY >= 0 && subX < width && subY < height) {
                    if (mirror) {
                        target = input[width - subX - 1 + subY * width];
                    } else {
                        target = input[subX + subY * width];
                    }
                }

                ItemStack slot = StackUtil.asNonNull(inv.getStackInRowAndColumn(x, y));

                if (target instanceof ItemStack) {
                    ItemStack targetStack = (ItemStack) target;
                    if (!OreDictionary.itemMatches(targetStack, slot, false)) {
                        return false;
                    }
                    if (!StackUtil.doesStackNbtMatch(targetStack, slot)) {
                        return false;
                    }
                } else if (target instanceof List) {
                    boolean matched = false;

                    Iterator<ItemStack> itr = ((List<ItemStack>) target).iterator();
                    while (itr.hasNext() && !matched) {
                        matched = OreDictionary.itemMatches(StackUtil.asNonNull(itr.next()), slot, false);
                    }

                    if (!matched) {
                        return false;
                    }
                } else if (target == null && !slot.isEmpty()) {
                    return false;
                }
            }
        }

        return true;
    }
}
