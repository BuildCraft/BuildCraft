/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.inventory;

import buildcraft.api.core.IStackFilter;
import buildcraft.api.inventory.IItemTransactor.IItemExtractable;
import buildcraft.lib.misc.StackUtil;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public class TransactorEntityItem implements IItemExtractable {

    private final EntityItem entity;

    public TransactorEntityItem(EntityItem entity) {
        this.entity = entity;
    }

    @Override
    @Nonnull
    public ItemStack extract(IStackFilter filter, int min, int max, boolean simulate) {
        if (entity.isDead) {
            return StackUtil.EMPTY;
        }
        ItemStack current = entity.getEntityItem();
        if (current.isEmpty() || current.getCount() < min || min > 1 || max < 1 || max < min) {
            return StackUtil.EMPTY;
        }
        if (filter.matches(current)) {
            ItemStack extracted = simulate ? current.copy().splitStack(max) : current.splitStack(max);
            if (!simulate) {
                if (current.getCount() == 0) {
                    entity.setDead();
                } else {
                    entity.setEntityItemStack(current);
                }
            }
            return extracted;
        } else {
            return StackUtil.EMPTY;
        }
    }
}
