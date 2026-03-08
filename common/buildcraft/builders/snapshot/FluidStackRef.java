/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.snapshot;

import net.minecraft.nbt.INBT;
import net.minecraft.nbt.IntNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Objects;
import java.util.Optional;

public class FluidStackRef {
    private final NbtRef<StringNBT> fluid;
    private final NbtRef<IntNBT> amount;

    public FluidStackRef(NbtRef<StringNBT> fluid, NbtRef<IntNBT> amount) {
        this.fluid = fluid;
        this.amount = amount;
    }

    public FluidStack get(INBT nbt) {
        return new FluidStack(
                Objects.requireNonNull(
//                        FluidRegistry.getFluid(
                        ForgeRegistries.FLUIDS.getValue(
                                new ResourceLocation(
                                        fluid
                                                .get(nbt)
                                                .orElseThrow(NullPointerException::new)
//                                                .getString()
                                                .getAsString()
                                )
                        )
                ),
                Optional.ofNullable(amount)
                        .flatMap(ref -> ref.get(nbt))
//                        .map(IntTag::getInt)
                        .map(IntNBT::getAsInt)
//                        .orElse(Fluid.BUCKET_VOLUME)
                        .orElse(FluidAttributes.BUCKET_VOLUME)
        );
    }
}
