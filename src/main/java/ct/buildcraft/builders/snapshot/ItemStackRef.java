/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.builders.snapshot;

import java.util.Objects;
import java.util.Optional;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

public class ItemStackRef {
    private final NbtRef<StringTag> item;
    private final NbtRef<IntTag> amount;
    private final NbtRef<CompoundTag> tagCompound;

    public ItemStackRef(NbtRef<StringTag> item,
                        NbtRef<IntTag> amount,
                        //NbtRef<IntTag> meta,
                        NbtRef<CompoundTag> tagCompound) {
        this.item = item;
        this.amount = amount;
        //this.meta = meta;
        this.tagCompound = tagCompound;
    }

    public ItemStack get(Tag nbt) {
        ItemStack itemStack = new ItemStack(
            Objects.requireNonNull(
            		
            		ForgeRegistries.ITEMS.getValue(new ResourceLocation(//TODO Check!
                    item
                        .get(nbt)
                        .orElseThrow(NullPointerException::new)
                        .getAsString()
                ))
            ),
            Optional.ofNullable(amount)
                .flatMap(ref -> ref.get(nbt))
                .map(IntTag::getAsInt)
                .orElse(1)/*,
            Optional.ofNullable(meta)
                .flatMap(ref -> ref.get(nbt))
                .map(IntTag::getAsInt)
                .orElse(0)*/
        );
        Optional.ofNullable(tagCompound)
            .flatMap(ref -> ref.get(nbt))
            .ifPresent(itemStack::setTag);
        return itemStack;
    }
}
