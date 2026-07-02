/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.lib.inventory;

import javax.annotation.Nonnull;

import ct.buildcraft.api.core.IStackFilter;
import ct.buildcraft.api.inventory.IItemTransactor.IItemExtractable;
import ct.buildcraft.lib.misc.EntityUtil;
import ct.buildcraft.lib.misc.StackUtil;

import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;

public class TransactorEntityArrow implements IItemExtractable {

    private final AbstractArrow entity;

    public TransactorEntityArrow(AbstractArrow entity) {
        this.entity = entity;
    }

    @Nonnull
    @Override
    public ItemStack extract(IStackFilter filter, int min, int max, boolean simulate) {
        if (entity.isRemoved() || entity.pickup != AbstractArrow.Pickup.ALLOWED || min > 1 || max < 1 || max < min) {
            return StackUtil.EMPTY;
        }

        ItemStack stack = EntityUtil.getArrowStack(entity);
        if (!simulate) {
            entity.invalidateCaps();
        }
        return stack;
    }
}
