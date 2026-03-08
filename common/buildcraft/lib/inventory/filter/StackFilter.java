/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.inventory.filter;

import buildcraft.api.core.IStackFilter;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.FurnaceTileEntity;

import javax.annotation.Nonnull;

/** This interface is used with several of the functions in IItemTransfer to provide a convenient means of dealing with
 * entire classes of items without having to specify each item individually. */
public enum StackFilter implements IStackFilter {

    ALL {
        @Override
        public boolean matches(@Nonnull ItemStack stack) {
            return true;
        }
    },
    FUEL {
        @Override
        public boolean matches(@Nonnull ItemStack stack) {
//            return FurnaceBlockEntity.getItemBurnTime(stack) > 0;
            return FurnaceTileEntity.isFuel(stack);
        }
    };

    @Override
    public abstract boolean matches(@Nonnull ItemStack stack);
}
