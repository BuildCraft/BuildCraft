/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.tile.item;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.collection.DefaultedList;

// STUB(R.Chen): the full Forge IItemHandler/IItemHandlerModifiable manager (named slot groups, insertion
// filters, capability provider wiring, the ItemHandlerSimple/Wrapped* family) is deferred to the
// Transfer-API item-handler migration pass. Only the no-op surface referenced by TileBC_Neptune
// (constructor + addDrops + serialize/deserialize) is retained so the tile base compiles. Real slot
// storage is restored when the Transfer-API item layer lands; see BCItemStorage.
public class ItemHandlerManager {

    @SuppressWarnings("unused")
    private final StackChangeCallback defaultCallback;

    public ItemHandlerManager(StackChangeCallback defaultCallback) {
        this.defaultCallback = defaultCallback;
    }

    public void addDrops(DefaultedList<ItemStack> toDrop) {
        // STUB(R.Chen): no slots are tracked yet, so there is nothing to drop.
    }

    public NbtCompound serializeNBT() {
        return new NbtCompound();
    }

    public void deserializeNBT(NbtCompound nbt) {
        // STUB(R.Chen): no slots are tracked yet.
    }
}
