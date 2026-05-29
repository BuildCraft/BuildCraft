/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */
package buildcraft.lib.tile.item;

import net.minecraft.item.ItemStack;

// TODO(R.Chen): IItemHandlerFiltered (api.inventory) dropped — Forge IItemHandler parent not migrated.
// Restore the interface once the full item-handler Transfer-API pass lands.
public class ItemHandlerFiltered extends ItemHandlerSimple {

    private final IItemHandlerAdv filter;
    private final boolean emptyIsAnything;

    public ItemHandlerFiltered(IItemHandlerAdv filter, boolean emptyIsAnything) {
        super(filter.getSlots());
        this.emptyIsAnything = emptyIsAnything;
        this.filter = filter;
        setChecker((slot, stack) -> {
            ItemStack inSlot = filter.getStackInSlot(slot);
            if (inSlot.isEmpty()) {
                return emptyIsAnything;
            } else {
                return ItemStack.canCombine(stack, inSlot);
            }
        });
    }

    @Override
    public int getSlotLimit(int slot) {
        if (emptyIsAnything || !getFilter(slot).isEmpty()) {
            return super.getSlotLimit(slot);
        } else {
            return 0;
        }
    }

    public ItemStack getFilter(int slot) {
        ItemStack current = getStackInSlot(slot);
        if (!current.isEmpty()) {
            return current;
        }
        return filter.getStackInSlot(slot);
    }
}
