/*
 * Copyright (c) 2020 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;

import javax.annotation.Nonnull;
import java.util.Objects;

/** Predicate that compares values of specified NBT keys subset. */
public class StackNbtMatcher implements StackMatchingPredicate {
    private String[] keys;

    public StackNbtMatcher(@Nonnull String... keys) {
        this.keys = keys;
    }

    @Override
    public boolean isMatching(@Nonnull ItemStack base, @Nonnull ItemStack comparison) {
        CompoundNBT baseNBT = base.getTag();
        CompoundNBT comparisonNBT = comparison.getTag();

        for (String key : keys) {
            INBT baseValue = baseNBT != null ? baseNBT.get(key) : null;
            INBT comparisonValue = comparisonNBT != null ? comparisonNBT.get(key) : null;
            if (!Objects.equals(baseValue, comparisonValue)) {
                return false;
            }
        }

        return true;
    }
}
