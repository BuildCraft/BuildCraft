/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.snapshot;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

import buildcraft.lib.compat.FluidStackBC;

@SuppressWarnings("WeakerAccess")
public class RequiredExtractorConstant extends RequiredExtractor {
    @SerializedName("items")
    private List<ItemStackRef> itemRefs = Collections.emptyList();
    @SerializedName("fluids")
    private List<FluidStackRef> fluidRefs = Collections.emptyList();

    @Nonnull
    @Override
    public List<ItemStack> extractItemsFromBlock(@Nonnull BlockState blockState, @Nullable NbtCompound tileNbt) {
        return Collections.unmodifiableList(
            itemRefs.stream()
                .map(ref -> ref.get(tileNbt))
                .collect(Collectors.toList())
        );
    }

    @Nonnull
    @Override
    public List<FluidStackBC> extractFluidsFromBlock(@Nonnull BlockState blockState, @Nullable NbtCompound tileNbt) {
        return Collections.unmodifiableList(
            fluidRefs.stream()
                .map(ref -> ref.get(tileNbt))
                .collect(Collectors.toList())
        );
    }

    @Nonnull
    @Override
    public List<ItemStack> extractItemsFromEntity(@Nonnull NbtCompound entityNbt) {
        return Collections.unmodifiableList(
            itemRefs.stream()
                .map(ref -> ref.get(entityNbt))
                .collect(Collectors.toList())
        );
    }

    @Nonnull
    @Override
    public List<FluidStackBC> extractFluidsFromEntity(@Nonnull NbtCompound entityNbt) {
        return Collections.unmodifiableList(
            fluidRefs.stream()
                .map(ref -> ref.get(entityNbt))
                .collect(Collectors.toList())
        );
    }
}
