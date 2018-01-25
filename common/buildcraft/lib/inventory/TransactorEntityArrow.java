/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.inventory;

import javax.annotation.Nullable;

import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityArrow.PickupStatus;
import net.minecraft.item.ItemStack;

import buildcraft.api.core.IStackFilter;
import buildcraft.api.inventory.IItemTransactor.IItemExtractable;

import buildcraft.lib.misc.EntityUtil;

public class TransactorEntityArrow implements IItemExtractable {

    private final EntityArrow entity;

    public TransactorEntityArrow(EntityArrow entity) {
        this.entity = entity;
    }

    @Nullable
    @Override
    public ItemStack extract(IStackFilter filter, int min, int max, boolean simulate) {
        if (entity.isDead || entity.pickupStatus != PickupStatus.ALLOWED || min > 1 || max < 1 || max < min) {
            return null;
        }

        ItemStack stack = EntityUtil.getArrowStack(entity);
        if (!simulate) {
            entity.setDead();
        }
        return stack;
    }
}
