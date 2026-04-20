/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.lib.inventory.filter;

import javax.annotation.Nonnull;

import ct.buildcraft.api.core.IStackFilter;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.ForgeHooks;

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
            return ForgeHooks.getBurnTime(stack, null) > 0;
        }
    };

    @Override
    public abstract boolean matches(@Nonnull ItemStack stack);
}
