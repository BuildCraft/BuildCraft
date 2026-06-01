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
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtInt;
import net.minecraft.nbt.NbtString;

public class ItemStackRef {
    private final NbtRef<NbtString> item;
    private final NbtRef<NbtInt> amount;
    private final NbtRef<NbtInt> meta;
    private final NbtRef<NbtCompound> tagCompound;

    public ItemStackRef(NbtRef<NbtString> item,
                        NbtRef<NbtInt> amount,
                        NbtRef<NbtInt> meta,
                        NbtRef<NbtCompound> tagCompound) {
        this.item = item;
        this.amount = amount;
        this.meta = meta;
        this.tagCompound = tagCompound;
    }

    public ItemStack get(NbtElement nbt) {
        ItemStack itemStack = new ItemStack(Objects.requireNonNull(
                Item.getByNameOrId(
                    item
                        .get(nbt)
                        .orElseThrow(NullPointerException::new)
                        .getString()
                )
            ), Optional.ofNullable(amount)
                .flatMap(ref -> ref.get(nbt))
                .map(NbtInt::getInt)
                .orElse(1));
        Optional.ofNullable(tagCompound)
            .flatMap(ref -> ref.get(nbt))
            .ifPresent(itemStack::setTagCompound);
        return itemStack;
    }
}
