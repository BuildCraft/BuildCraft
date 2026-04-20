/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.builders.snapshot;

import java.util.Objects;
import java.util.Optional;

import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.registries.ForgeRegistries;

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
                ForgeRegistries.FLUIDS.getValue(new ResourceLocation(//TODO check!
                    fluid
                        .get(nbt)
                        .orElseThrow(NullPointerException::new)
                        .getAsString()
                ))
            ),
            Optional.ofNullable(amount)
                .flatMap(ref -> ref.get(nbt))
                .map(IntTag::getAsInt)
                .orElse(FluidType.BUCKET_VOLUME)
        );
    }
}
