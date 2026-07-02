/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.lib.net.cache;

import java.io.IOException;
import java.util.Objects;

import ct.buildcraft.lib.misc.StackUtil;

import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenCustomHashMap;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

// We use ItemStackKey here because ItemStack doesn't implement hashCode and equals
public class NetworkedItemStackCache extends NetworkedObjectCache<ItemStack> {

    public NetworkedItemStackCache() {
        super(StackUtil.EMPTY);
    }

    @Override
    protected Object2IntMap<ItemStack> createObject2IntMap() {
        return new Object2IntOpenCustomHashMap<>(new Hash.Strategy<ItemStack>() {
            @Override
            public int hashCode(ItemStack o) {
                if (o == null || o.isEmpty()) {
                    return 0;
                }
                return Objects.hash(o.getItem(), o.getTag());
            }

            @Override
            public boolean equals(ItemStack a, ItemStack b) {
                if (a == null || b == null) {
                    return a == b;
                }
                return StackUtil.canMerge(a, b);
            }
        });
    }

    @Override
    protected ItemStack copyOf(ItemStack object) {
        return object == null ? null : object.copy();
    }

    @Override
    protected void writeObject(ItemStack obj, FriendlyByteBuf buffer) {
        if (obj == null || obj.isEmpty()) {
            buffer.writeBoolean(false);
        } else {
        	buffer.writeBoolean(true);
        	buffer.writeNbt(obj.serializeNBT());
        }
    }

    @Override
    protected ItemStack readObject(FriendlyByteBuf buffer) throws IOException {
        if (buffer.readBoolean()) {
            return ItemStack.of(buffer.readNbt());
        } else {
            return ItemStack.EMPTY;
        }
    }

    @Override
    protected String getCacheName() {
        return "ItemStack";
    }
}
