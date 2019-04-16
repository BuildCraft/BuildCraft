/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.net.cache;

import java.io.IOException;
import java.util.Objects;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import buildcraft.lib.net.PacketBufferBC;

import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenCustomHashMap;

public class NetworkedFluidStackCache extends NetworkedObjectCache<FluidStack> {
    private static final int FLUID_AMOUNT = 1;

    public NetworkedFluidStackCache() {
        // Use water for our base stack as it might not be too bad of an assumption
        super(new FluidStack(FluidRegistry.WATER, FLUID_AMOUNT));
    }

    @Override
    protected Object2IntMap<FluidStack> createObject2IntMap() {
        return new Object2IntOpenCustomHashMap<>(new Hash.Strategy<FluidStack>() {
            @Override
            public int hashCode(FluidStack o) {
                if (o == null) {
                    return 0;
                }
                return Objects.hash(o.getFluid(), o.tag);
            }

            @Override
            public boolean equals(FluidStack a, FluidStack b) {
                if (a == null || b == null) {
                    return a == b;
                }
                return a.getFluid() == b.getFluid() //
                    && Objects.equals(a.tag, b.tag);
            }
        });
    }

    @Override
    protected FluidStack copyOf(FluidStack object) {
        return object.copy();
    }

    @Override
    protected void writeObject(FluidStack obj, PacketBufferBC buffer) {
        Fluid f = obj.getFluid();
        buffer.writeString(FluidRegistry.getFluidName(f));
        if (obj.tag == null) {
            buffer.writeBoolean(false);
        } else {
            buffer.writeBoolean(true);
            buffer.writeCompoundTag(obj.tag);
        }
    }

    @Override
    protected FluidStack readObject(PacketBufferBC buffer) throws IOException {
        Fluid fluid = FluidRegistry.getFluid(buffer.readString(255));
        FluidStack stack = new FluidStack(fluid, FLUID_AMOUNT);
        if (buffer.readBoolean()) {
            stack.tag = buffer.readCompoundTag();
        }
        return stack;
    }

    @Override
    protected String getCacheName() {
        return "FluidStack";
    }
}
