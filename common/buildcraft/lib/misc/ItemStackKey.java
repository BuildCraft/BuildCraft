/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc;

import java.util.Objects;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;

public class ItemStackKey {
    public static final ItemStackKey EMPTY = new ItemStackKey(ItemStack.EMPTY);

    public final @Nonnull ItemStack baseStack;
    private final int hash;

    public ItemStackKey(@Nonnull ItemStack stack) {
        if (stack.isEmpty()) {
            baseStack = ItemStack.EMPTY;
            hash = 0;
        } else {
            this.baseStack = stack.copy();
            // getMetadata() and serializeNBT() were removed in 1.13+.
            // Hash on item identity + NBT tag hashCode (null NBT → 0).
            this.hash = Objects.hash(this.baseStack.getItem(),
                this.baseStack.getNbt() != null ? this.baseStack.getNbt().hashCode() : 0);
        }
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null) return false;
        if (obj.getClass() != this.getClass()) return false;
        ItemStackKey other = (ItemStackKey) obj;
        if (hash != other.hash) return false;
        if (baseStack.getItem() != other.baseStack.getItem()) {
            return false;
        }
        // getMetadata() was removed in 1.13 (item flattening). Item identity is now sufficient.
        // serializeNBT() was Forge-only; compare via getNbt() which returns null when no tag is present.
        net.minecraft.nbt.NbtCompound a = baseStack.getNbt();
        net.minecraft.nbt.NbtCompound b = other.baseStack.getNbt();
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.equals(b);
    }

    @Override
    public String toString() {
        return "[ItemStackKey " + baseStack + "]";
    }
}
