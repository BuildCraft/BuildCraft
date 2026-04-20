/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.lib.misc;

import javax.annotation.Nonnull;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public class ItemStackKey {
    public static final ItemStackKey EMPTY = new ItemStackKey(StackUtil.EMPTY);

    public final @Nonnull ItemStack baseStack;
    private final int hash;

    public ItemStackKey(@Nonnull ItemStack stack) {
        if (stack.isEmpty()) {
            baseStack = StackUtil.EMPTY;
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
        if (hash != other.hash) return false;
        if (baseStack.getItem() != other.baseStack.getItem()) {
            return false;
        }
        CompoundTag tag1 = baseStack.getTag();
        CompoundTag tag2 = other.baseStack.getTag();
        boolean flag = tag1 == null;
        if ((flag&&tag2!=null)||(!flag&&tag1.equals(tag2))) {
            return false;
        }
        return baseStack.serializeNBT().equals(other.baseStack.serializeNBT());
    }

    @Override
    public String toString() {
        return "[ItemStackKey " + baseStack + "]";
    }
}
