/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.tile.item;

import javax.annotation.Nullable;

import net.minecraft.item.ItemStack;

import net.minecraftforge.items.IItemHandlerModifiable;

public class WrappedItemHandlerInsert extends DelegateItemHandler {

    public WrappedItemHandlerInsert(IItemHandlerModifiable delegate) {
        super(delegate);
    }

    @Override
    @Nullable
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        return null;
    }
}
