/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
// STUB(R.Chen): FluidUtilBC deferred — Forge FluidUtil API replaced in 1.20.1
package buildcraft.lib.misc;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;

import buildcraft.lib.compat.FluidStackBC;
import buildcraft.lib.fluid.Tank;

public class FluidUtilBC {

    public static boolean interactWithFluidHandler(@Nonnull ItemStack stack, Tank tank, boolean doAction) {
        return false;
    }

    public static FluidStackBC getFluidContained(ItemStack stack) {
        return null;
    }
}
