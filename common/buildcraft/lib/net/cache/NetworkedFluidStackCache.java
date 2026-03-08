/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.net.cache;

import buildcraft.lib.net.PacketBufferBC;
import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenCustomHashMap;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraftforge.fluids.FluidStack;

import java.io.IOException;
import java.util.Objects;

public class NetworkedFluidStackCache extends NetworkedObjectCache<FluidStack> {
    private static final int FLUID_AMOUNT = 1;

    public NetworkedFluidStackCache() {
        // Use water for our base stack as it might not be too bad of an assumption
        super(new FluidStack(Fluids.WATER, FLUID_AMOUNT));
    }

    @Override
    protected Object2IntMap<FluidStack> createObject2IntMap() {
        return new Object2IntOpenCustomHashMap<>(new Hash.Strategy<FluidStack>() {
            @Override
            public int hashCode(FluidStack o) {
                if (o == null) {
                    return 0;
                }
                return Objects.hash(o.getRawFluid(), o.getTag());
            }

            @Override
            public boolean equals(FluidStack a, FluidStack b) {
                if (a == null || b == null) {
                    return a == b;
                }
                return a.getRawFluid() == b.getRawFluid() //
                        && Objects.equals(a.getTag(), b.getTag());
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
//        buffer.writeString(FluidRegistry.getFluidName(f));
        buffer.writeRegistryId(f); // Calen: just like FluidStack#writeToPacket
//        if (obj.tag == null)
        if (obj.getTag() == null) {
            buffer.writeBoolean(false);
        } else {
            buffer.writeBoolean(true);
//            buffer.writeCompoundTag(obj.tag);
            buffer.writeNbt(obj.getTag());
        }
    }

    @Override
    protected FluidStack readObject(PacketBufferBC buffer) throws IOException {
//        Fluid fluid = FluidRegistry.getFluid(buffer.readString(255));
        Fluid fluid = buffer.readRegistryId(); // Calen: just like FluidStack#readFromPacket
        FluidStack stack = new FluidStack(fluid, FLUID_AMOUNT);
        if (buffer.readBoolean()) {
//            stack.tag = buffer.readCompoundTag();
            stack.setTag(buffer.readNbt());
        }
        return stack;
    }

    @Override
    protected String getCacheName() {
        return "FluidStack";
    }
}
