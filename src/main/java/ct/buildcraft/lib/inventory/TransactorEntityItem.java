/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.lib.inventory;

import javax.annotation.Nonnull;

import ct.buildcraft.api.core.IStackFilter;
import ct.buildcraft.api.inventory.IItemTransactor.IItemExtractable;
import ct.buildcraft.lib.misc.StackUtil;

import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

public class TransactorEntityItem implements IItemExtractable {

    private final ItemEntity entity;

    public TransactorEntityItem(ItemEntity entity) {
        this.entity = entity;
    }

    @Override
    @Nonnull
    public ItemStack extract(IStackFilter filter, int min, int max, boolean simulate) {
        if (entity.isRemoved()) {
            return StackUtil.EMPTY;
        }
        if (min < 1) {
            min = 1;
        }
        if (max < min) {
            return StackUtil.EMPTY;
        }
        ItemStack current = entity.getItem();
        if (current.isEmpty() || current.getCount() < min) {
            return StackUtil.EMPTY;
        }
        if (filter.matches(current)) {
            current = current.copy();
            ItemStack extracted = current.split(max);
            if (!simulate) {
                if (current.getCount() == 0) {
                    entity.setRemoved(RemovalReason.DISCARDED);
                } else {
                    entity.setItem(current);
                }
            }
            return extracted;
        } else {
            return StackUtil.EMPTY;
        }
    }

    @Override
    public String toString() {
        return entity.toString();
    }
}
