/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.snapshot;

import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Objects;
import java.util.Optional;

public class FluidStackRef {
    private final NbtRef<StringTag> fluid;
    private final NbtRef<IntTag> amount;

    public FluidStackRef(NbtRef<StringTag> fluid, NbtRef<IntTag> amount) {
        this.fluid = fluid;
        this.amount = amount;
    }

    public FluidStack get(Tag nbt) {
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
                        .map(IntTag::getAsInt)
//                        .orElse(Fluid.BUCKET_VOLUME)
                        .orElse(FluidType.BUCKET_VOLUME)
        );
    }
}
