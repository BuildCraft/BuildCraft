/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.lib.net.cache;

import java.io.IOException;
import java.util.Objects;

import com.mojang.logging.LogUtils;

import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenCustomHashMap;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

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
                return Objects.hash(o.getFluid(), o.getTag());
            }

            @Override
            public boolean equals(FluidStack a, FluidStack b) {
                if (a == null || b == null) {
                    return a == b;
                }
                return a.getFluid() == b.getFluid() //
                    && Objects.equals(a.getTag(), b.getTag());
            }
        });
    }

    @Override
    protected FluidStack copyOf(FluidStack object) {
        return object.copy();
    }

    @Override
    protected void writeObject(FluidStack obj, FriendlyByteBuf buffer) {
        Fluid f = obj.getFluid();
        buffer.writeUtf(ForgeRegistries.FLUIDS.getKey(f).toString(),64);
        if (obj.getTag() == null) {
            buffer.writeBoolean(false);
        } else {
            buffer.writeBoolean(true);
            buffer.writeNbt(obj.getTag());
        }
    }

    @Override
    protected FluidStack readObject(FriendlyByteBuf buffer) throws IOException {

        //Debug start
        String temp = buffer.readUtf(64);
        LogUtils.getLogger().info("trying to deserialize packet, result : " + temp);
        Fluid fluid = ForgeRegistries.FLUIDS.getValue(new ResourceLocation(temp));
        //Debug end
        
        //Fluid fluid = ForgeRegistries.FLUIDS.getValue(new ResourceLocation(buffer.readUtf(64)));
        FluidStack stack = new FluidStack(fluid, FLUID_AMOUNT);
        if (buffer.readBoolean()) {
            stack.setTag(buffer.readNbt());
        }
        return stack;
    }

    @Override
    protected String getCacheName() {
        return "FluidStack";
    }
}
