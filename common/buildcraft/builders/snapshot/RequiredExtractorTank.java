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

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;

import net.minecraftforge.fluids.FluidStack;

public class RequiredExtractorTank extends RequiredExtractor {
    private NbtPath path = null;

    @Nonnull
    @Override
    public List<FluidStack> extractFluidsFromBlock(@Nonnull IBlockState blockState, @Nullable NBTTagCompound tileNbt) {
        return Optional.ofNullable(path.get(tileNbt))
            .map(NBTTagCompound.class::cast)
            .map(nbt -> !nbt.hasKey("Empty") ? FluidStack.loadFluidStackFromNBT(nbt) : null)
            .map(Collections::singletonList)
            .orElseGet(Collections::emptyList);
    }

    @Nonnull
    @Override
    public List<FluidStack> extractFluidsFromEntity(@Nonnull NBTTagCompound entityNbt) {
        return Optional.ofNullable(path.get(entityNbt))
            .map(NBTTagCompound.class::cast)
            .map(nbt -> !nbt.hasKey("Empty") ? FluidStack.loadFluidStackFromNBT(nbt) : null)
            .map(Collections::singletonList)
            .orElseGet(Collections::emptyList);
    }
}
