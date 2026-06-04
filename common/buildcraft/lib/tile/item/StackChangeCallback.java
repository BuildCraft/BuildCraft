/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.tile.item;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;

// STUB(R.Chen): first arg was Forge IItemHandlerModifiable; reduced to Object until the Transfer-API
// item-handler layer is migrated (matches TileBC_Neptune#onSlotChange's stubbed signature).
@FunctionalInterface
public interface StackChangeCallback {
    void onStackChange(Object itemHandler, int slot, @Nonnull ItemStack before, @Nonnull ItemStack after);
}
