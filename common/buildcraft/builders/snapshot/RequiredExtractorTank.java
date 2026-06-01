/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.snapshot;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.NbtCompound;

import buildcraft.lib.compat.FluidStackBC;

public class RequiredExtractorTank extends RequiredExtractor {
    private NbtPath path = null;

    @Nonnull
    @Override
    public List<FluidStackBC> extractFluidsFromBlock(@Nonnull BlockState blockState, @Nullable NbtCompound tileNbt) {
        return Optional.ofNullable(path.get(tileNbt))
            .map(NbtCompound.class::cast)
            .map(nbt -> !nbt.contains("Empty") ? FluidStackBC.loadFluidStackFromNBT(nbt) : null)
            .map(Collections::singletonList)
            .orElseGet(Collections::emptyList);
    }

    @Nonnull
    @Override
    public List<FluidStackBC> extractFluidsFromEntity(@Nonnull NbtCompound entityNbt) {
        return Optional.ofNullable(path.get(entityNbt))
            .map(NbtCompound.class::cast)
            .map(nbt -> !nbt.contains("Empty") ? FluidStackBC.loadFluidStackFromNBT(nbt) : null)
            .map(Collections::singletonList)
            .orElseGet(Collections::emptyList);
    }
}
