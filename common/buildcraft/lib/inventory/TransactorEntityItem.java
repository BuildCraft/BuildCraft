/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.inventory;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;

import buildcraft.api.core.IStackFilter;
import buildcraft.api.inventory.IItemTransactor.IItemExtractable;

public class TransactorEntityItem implements IItemExtractable {

    private final EntityItem entity;

    public TransactorEntityItem(EntityItem entity) {
        this.entity = entity;
    }

    @Override
    public ItemStack extract(IStackFilter filter, int min, int max, boolean simulate) {
        if (entity.isDead) {
            return null;
        }
        if (min < 1) {
            min = 1;
        }
        if (max < min) {
            return null;
        }
        ItemStack current = entity.getEntityItem();
        if (current == null || current.stackSize < min) {
            return null;
        }
        if (filter.matches(current)) {
            current = current.copy();
            ItemStack extracted = current.splitStack(max);
            if (!simulate) {
                if (current.stackSize == 0) {
                    entity.setDead();
                } else {
                    entity.setEntityItemStack(current);
                }
            }
            return extracted;
        } else {
            return null;
        }
    }

    @Override
    public String toString() {
        return entity.toString();
    }
}
