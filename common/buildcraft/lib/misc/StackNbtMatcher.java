/*
 * Copyright (c) 2020 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc;

import java.util.Objects;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtCompound;

/**
 * Predicate that compares values of specified NBT keys subset.
 */
public class StackNbtMatcher implements StackMatchingPredicate {
    private String[] keys;

    public StackNbtMatcher(@Nonnull String ...keys) {
        this.keys = keys;
    }

    @Override
    public boolean isMatching(@Nonnull ItemStack base, @Nonnull ItemStack comparison) {
        NbtCompound baseNBT = base.getTagCompound();
        NbtCompound comparisonNBT = comparison.getTagCompound();

        for (String key : keys) {
            NbtElement baseValue = baseNBT != null ? baseNBT.getTag(key) : null;
            NbtElement comparisonValue = comparisonNBT != null ? comparisonNBT.getTag(key) : null;
            if (!Objects.equals(baseValue, comparisonValue)) {
                return false;
            }
        }

        return true;
    }
}
