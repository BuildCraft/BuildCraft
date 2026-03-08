/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.snapshot;

import com.google.gson.annotations.SerializedName;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("WeakerAccess")
public class RequiredExtractorConstant extends RequiredExtractor {
    @SerializedName("items")
    private List<ItemStackRef> itemRefs = Collections.emptyList();
    @SerializedName("fluids")
    private List<FluidStackRef> fluidRefs = Collections.emptyList();

    @Nonnull
    @Override
    public List<ItemStack> extractItemsFromBlock(@Nonnull BlockState blockState, @Nullable CompoundNBT tileNbt) {
        return Collections.unmodifiableList(
                itemRefs.stream()
                        .map(ref -> ref.get(tileNbt))
                        .collect(Collectors.toList())
        );
    }

    @Nonnull
    @Override
    public List<FluidStack> extractFluidsFromBlock(@Nonnull BlockState blockState, @Nullable CompoundNBT tileNbt) {
        return Collections.unmodifiableList(
                fluidRefs.stream()
                        .map(ref -> ref.get(tileNbt))
                        .collect(Collectors.toList())
        );
    }

    @Nonnull
    @Override
    public List<ItemStack> extractItemsFromEntity(@Nonnull CompoundNBT entityNbt) {
        return Collections.unmodifiableList(
                itemRefs.stream()
                        .map(ref -> ref.get(entityNbt))
                        .collect(Collectors.toList())
        );
    }

    @Nonnull
    @Override
    public List<FluidStack> extractFluidsFromEntity(@Nonnull CompoundNBT entityNbt) {
        return Collections.unmodifiableList(
                fluidRefs.stream()
                        .map(ref -> ref.get(entityNbt))
                        .collect(Collectors.toList())
        );
    }
}
