/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.snapshot;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class RequiredExtractorTank extends RequiredExtractor {
    private NbtPath path = null;

    @Nonnull
    @Override
    public List<FluidStack> extractFluidsFromBlock(@Nonnull BlockState blockState, @Nullable CompoundNBT tileNbt) {
        return Optional.ofNullable(path.get(tileNbt))
                .map(CompoundNBT.class::cast)
                .map(nbt -> !nbt.contains("Empty") ? FluidStack.loadFluidStackFromNBT(nbt) : null)
                .map(Collections::singletonList)
                .orElseGet(Collections::emptyList);
    }

    @Nonnull
    @Override
    public List<FluidStack> extractFluidsFromEntity(@Nonnull CompoundNBT entityNbt) {
        return Optional.ofNullable(path.get(entityNbt))
                .map(CompoundNBT.class::cast)
                .map(nbt -> !nbt.contains("Empty") ? FluidStack.loadFluidStackFromNBT(nbt) : null)
                .map(Collections::singletonList)
                .orElseGet(Collections::emptyList);
    }
}
