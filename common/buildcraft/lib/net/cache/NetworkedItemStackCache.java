/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.net.cache;

import java.io.IOException;

import net.minecraft.item.ItemStack;

import buildcraft.lib.misc.ItemStackKey;
import buildcraft.lib.misc.StackUtil;
import buildcraft.lib.net.PacketBufferBC;

// We use ItemStackKey here because ItemStack doesn't implement hashCode and equals
public class NetworkedItemStackCache extends NetworkedObjectCache<ItemStackKey> {

    public NetworkedItemStackCache() {
        super(new ItemStackKey(null));
    }

    @Override
    protected ItemStackKey getCanonical(ItemStackKey obj) {
        if (obj.baseStack == null) {
            return ItemStackKey.EMPTY;
        }
        ItemStack stack = obj.baseStack.copy();
        stack.stackSize = 1;
        if (stack.hasTagCompound()) {
            stack.setTagCompound(StackUtil.stripNonFunctionNbt(stack));
        }
        return new ItemStackKey(stack);
    }

    @Override
    protected void writeObject(ItemStackKey obj, PacketBufferBC buffer) {
        if (obj.baseStack == null) {
            buffer.writeBoolean(false);
        } else {
            buffer.writeBoolean(true);
            buffer.writeItemStack(obj.baseStack);
        }
    }

    @Override
    protected ItemStackKey readObject(PacketBufferBC buffer) throws IOException {
        if (buffer.readBoolean()) {
            return new ItemStackKey(buffer.readItemStack());
        } else {
            return ItemStackKey.EMPTY;
        }
    }

    @Override
    protected String getCacheName() {
        return "ItemStack";
    }
}
