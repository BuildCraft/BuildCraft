/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc;

import javax.annotation.Nullable;

import net.minecraft.item.ItemStack;

public class ItemStackKey {
    public static final ItemStackKey EMPTY = new ItemStackKey(null);

    @Nullable
    public final ItemStack baseStack;
    private final int hash;

    public ItemStackKey(ItemStack stack) {
        if (stack == null) {
            baseStack = null;
            hash = 0;
        } else {
            this.baseStack = stack.copy();
            this.hash = StackUtil.hash(baseStack);
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
        return hash == other.hash && baseStack.serializeNBT().equals(other.baseStack.serializeNBT());
    }

    @Override
    public String toString() {
        return "[ItemStackKey " + baseStack + "]";
    }
}
