/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.net.cache;

import buildcraft.lib.misc.StackUtil;
import buildcraft.lib.net.PacketBufferBC;
import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenCustomHashMap;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;

import java.io.IOException;
import java.util.Objects;

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
    protected void writeObject(ItemStack obj, PacketBufferBC buffer) {
        if (obj == null || obj.isEmpty()) {
            buffer.writeBoolean(false);
        } else {
            buffer.writeBoolean(true);
            buffer.writeShort(Item.getId(obj.getItem()));
//            buffer.writeShort(obj.getMetadata());
            CompoundNBT tag = null;
//            if (obj.getItem().isDamageable(obj) || obj.getItem().getShareTag(obj))
            if (obj.getItem().isDamageable(obj)) {
                tag = obj.getItem().getShareTag(obj);
            }
            buffer.writeNbt(tag);
        }
    }

    @Override
    protected ItemStack readObject(PacketBufferBC buffer) throws IOException {
        if (buffer.readBoolean()) {
            Item item = Item.byId(buffer.readUnsignedShort());
//            int meta = buffer.readShort();
//            ItemStack stack = new ItemStack(item, 1, meta);
            ItemStack stack = new ItemStack(item, 1);
            stack.setTag(buffer.readNbt());
            return stack;
        } else {
            return ItemStack.EMPTY;
        }
    }

    @Override
    protected String getCacheName() {
        return "ItemStack";
    }
}
