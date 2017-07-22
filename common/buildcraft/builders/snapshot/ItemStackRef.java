/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.snapshot;

import java.util.Objects;
import java.util.Optional;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagString;

public class ItemStackRef {
    private final NbtRef<NBTTagString> item;
    private final NbtRef<NBTTagInt> amount;
    private final NbtRef<NBTTagInt> meta;

    public ItemStackRef(NbtRef<NBTTagString> item, NbtRef<NBTTagInt> amount, NbtRef<NBTTagInt> meta) {
        this.item = item;
        this.amount = amount;
        this.meta = meta;
    }

    public ItemStack get(NBTBase nbt) {
        return new ItemStack(
            Objects.requireNonNull(Item.getByNameOrId(item.get(nbt).getString())),
            Optional.ofNullable(amount).map(ref -> ref.get(nbt)).map(NBTTagInt::getInt).orElse(1),
            Optional.ofNullable(meta).map(ref -> ref.get(nbt)).map(NBTTagInt::getInt).orElse(0)
        );
    }
}
