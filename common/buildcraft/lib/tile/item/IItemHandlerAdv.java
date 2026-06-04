/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */

package buildcraft.lib.tile.item;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;

// STUB(R.Chen): Forge IItemHandler/IItemHandlerModifiable replaced with minimal stub.
// The Forge IItemHandler parent is dropped; methods are inlined so existing call sites compile.
// Full Transfer-API (fabric-transfer-api-v1) wiring deferred to the item-handler migration pass.
public interface IItemHandlerAdv extends StackInsertionChecker {

    int getSlots();

    @Nonnull
    ItemStack getStackInSlot(int slot);

    @Nonnull
    ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate);

    @Nonnull
    ItemStack extractItem(int slot, int amount, boolean simulate);

    int getSlotLimit(int slot);

    /** Replaces Forge IItemHandlerModifiable.setStackInSlot (used by ContainerBC_Neptune phantom-slot handling). */
    void setStackInSlot(int slot, @Nonnull ItemStack stack);
}
