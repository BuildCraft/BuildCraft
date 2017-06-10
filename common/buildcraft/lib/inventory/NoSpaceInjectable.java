/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.inventory;

import javax.annotation.Nonnull;

import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;

import buildcraft.api.transport.IInjectable;

public enum NoSpaceInjectable implements IInjectable {
    INSTANCE;

    @Override
    public boolean canInjectItems(EnumFacing from) {
        return false;
    }

    @Nonnull
    @Override
    public ItemStack injectItem(@Nonnull ItemStack stack, boolean doAdd, EnumFacing from, EnumDyeColor color, double speed) {
        return stack;
    }
}
