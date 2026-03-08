/*
 * Copyright (c) 2020 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc;

import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

@FunctionalInterface
public interface StackMatchingPredicate {
    boolean isMatching(@Nonnull ItemStack base, @Nonnull ItemStack comparison);
}
