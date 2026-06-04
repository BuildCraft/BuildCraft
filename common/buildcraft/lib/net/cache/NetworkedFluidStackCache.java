/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.net.cache;

import java.io.IOException;
import java.util.Objects;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;

import net.minecraft.fluid.Fluid;
import net.minecraft.nbt.NbtCompound;

import buildcraft.lib.compat.FluidRegistryBC;
import buildcraft.lib.compat.FluidStackBC;
import buildcraft.lib.net.PacketBufferBC;

import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenCustomHashMap;

public class NetworkedFluidStackCache extends NetworkedObjectCache<FluidStackBC> {
    private static final int FLUID_AMOUNT = 1;

    public NetworkedFluidStackCache() {
        super(new FluidStackBC(FluidRegistryBC.WATER, FLUID_AMOUNT));
    }

    @Override
    protected Object2IntMap<FluidStackBC> createObject2IntMap() {
        return new Object2IntOpenCustomHashMap<>(new Hash.Strategy<FluidStackBC>() {
            @Override
            public int hashCode(FluidStackBC o) {
                if (o == null) return 0;
                FluidVariant fv = o.getFluidVariant();
                return Objects.hash(fv.getFluid(), fv.getNbt());
            }

            @Override
            public boolean equals(FluidStackBC a, FluidStackBC b) {
                if (a == null || b == null) return a == b;
                FluidVariant fa = a.getFluidVariant();
                FluidVariant fb = b.getFluidVariant();
                return fa.getFluid() == fb.getFluid()
                    && Objects.equals(fa.getNbt(), fb.getNbt());
            }
        });
    }

    @Override
    protected FluidStackBC copyOf(FluidStackBC object) {
        return object.copy();
    }

    @Override
    protected void writeObject(FluidStackBC obj, PacketBufferBC buffer) {
        FluidVariant fv = obj.getFluidVariant();
        buffer.writeString(FluidRegistryBC.getFluidName(fv.getFluid()));
        NbtCompound nbt = fv.getNbt();
        if (nbt == null) {
            buffer.writeBoolean(false);
        } else {
            buffer.writeBoolean(true);
            buffer.writeCompoundTag(nbt);
        }
    }

    @Override
    protected FluidStackBC readObject(PacketBufferBC buffer) throws IOException {
        Fluid fluid = FluidRegistryBC.getFluid(buffer.readString(255));
        if (fluid == null) return FluidStackBC.EMPTY;
        FluidVariant fv;
        if (buffer.readBoolean()) {
            NbtCompound nbt = buffer.readCompoundTag();
            fv = FluidVariant.of(fluid, nbt);
        } else {
            fv = FluidVariant.of(fluid);
        }
        return FluidStackBC.of(fv, FLUID_AMOUNT);
    }

    @Override
    protected String getCacheName() {
        return "FluidStackBC";
    }
}
