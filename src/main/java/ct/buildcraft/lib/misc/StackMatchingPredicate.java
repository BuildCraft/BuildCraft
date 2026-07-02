/*
 * Copyright (c) 2020 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.lib.misc;

import javax.annotation.Nonnull;

import net.minecraft.world.item.ItemStack;

@FunctionalInterface
public interface StackMatchingPredicate {
    boolean isMatching(@Nonnull ItemStack base, @Nonnull ItemStack comparison);
}
