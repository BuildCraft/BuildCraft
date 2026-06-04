/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.snapshot;

import java.util.Objects;
import java.util.Optional;

import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtInt;
import net.minecraft.nbt.NbtString;

import net.minecraft.fluid.Fluid;
import buildcraft.lib.compat.FluidRegistryBC;
import buildcraft.lib.compat.FluidStackBC;

public class FluidStackRef {
    private final NbtRef<NbtString> fluid;
    private final NbtRef<NbtInt> amount;

    public FluidStackRef(NbtRef<NbtString> fluid, NbtRef<NbtInt> amount) {
        this.fluid = fluid;
        this.amount = amount;
    }

    public FluidStackBC get(NbtElement nbt) {
        return new FluidStackBC(
            Objects.requireNonNull(
                FluidRegistryBC.getFluid(
                    fluid
                        .get(nbt)
                        .orElseThrow(NullPointerException::new)
                        .asString()
                )
            ),
            Optional.ofNullable(amount)
                .flatMap(ref -> ref.get(nbt))
                .map(NbtInt::getInt)
                .orElse(Fluid.BUCKET_VOLUME)
        );
    }
}
