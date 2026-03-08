/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.inventory.filter;

import buildcraft.api.core.IStackFilter;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;

public class DelegatingItemHandlerFilter implements IStackFilter {
    private final ISingleStackFilter perStackFilter;
    private final IItemHandler handler;

    public DelegatingItemHandlerFilter(ISingleStackFilter perStackFilter, IItemHandler handler) {
        this.perStackFilter = perStackFilter;
        this.handler = handler;
    }

    @Override
    public boolean matches(@Nonnull ItemStack stack) {
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            if (perStackFilter.matches(handler.getStackInSlot(slot), stack)) {
                return true;
            }
        }
        return false;
    }
}
