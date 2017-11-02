/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.snapshot;

import java.util.Objects;
import java.util.Optional;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagString;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

public class FluidStackRef {
    private final NbtRef<NBTTagString> fluid;
    private final NbtRef<NBTTagInt> amount;

    public FluidStackRef(NbtRef<NBTTagString> fluid, NbtRef<NBTTagInt> amount) {
        this.fluid = fluid;
        this.amount = amount;
    }

    public FluidStack get(NBTBase nbt) {
        return new FluidStack(
            Objects.requireNonNull(
                FluidRegistry.getFluid(
                    fluid
                        .get(nbt)
                        .orElseThrow(NullPointerException::new)
                        .getString()
                )
            ),
            Optional.ofNullable(amount)
                .flatMap(ref -> ref.get(nbt))
                .map(NBTTagInt::getInt)
                .orElse(Fluid.BUCKET_VOLUME)
        );
    }
}
