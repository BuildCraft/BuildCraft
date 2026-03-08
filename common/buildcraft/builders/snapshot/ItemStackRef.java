/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.snapshot;


import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.IntNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Objects;
import java.util.Optional;

public class ItemStackRef {
    private final NbtRef<StringNBT> item;
    private final NbtRef<IntNBT> amount;
    //    private final NbtRef<IntTag> meta;
    private final NbtRef<CompoundNBT> tagCompound;

    public ItemStackRef(NbtRef<StringNBT> item,
                        NbtRef<IntNBT> amount,
//                        NbtRef<IntTag> meta,
                        NbtRef<CompoundNBT> tagCompound) {
        this.item = item;
        this.amount = amount;
//        this.meta = meta;
        this.tagCompound = tagCompound;
    }

    public ItemStack get(INBT nbt) {
        ItemStack itemStack = new ItemStack(
                Objects.requireNonNull(
//                        Item.getByNameOrId(
                        ForgeRegistries.ITEMS.getValue(new ResourceLocation(
                                item
                                        .get(nbt)
                                        .orElseThrow(NullPointerException::new)
                                        .getAsString()
                        ))
                ),
                Optional.ofNullable(amount)
                        .flatMap(ref -> ref.get(nbt))
                        .map(IntNBT::getAsInt)
                        .orElse(1)
//            Optional.ofNullable(meta)
//                .flatMap(ref -> ref.get(nbt))
//                .map(IntTag::getAsInt)
//                .orElse(0)
        );
        Optional.ofNullable(tagCompound)
                .flatMap(ref -> ref.get(nbt))
                .ifPresent(itemStack::setTag);
        return itemStack;
    }
}
