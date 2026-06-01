/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */
package buildcraft.lib.compat;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

/**
 * Compatibility shim replacing {@code net.minecraftforge.fluids.FluidRegistry}.
 *
 * Forge's FluidRegistry provided a global String↔Fluid map. Fabric uses
 * {@link Registries#FLUID} instead. This shim delegates to that registry so
 * unmigrated call sites compile without change.
 *
 * TODO(R.Chen): migrate all call sites to use Registries.FLUID directly.
 */
public final class FluidRegistryBC {

    private FluidRegistryBC() {}

    /** Forge compat constant: {@code FluidRegistry.WATER}. */
    public static final Fluid WATER = Fluids.WATER;

    /** Forge compat constant: {@code FluidRegistry.LAVA}. */
    public static final Fluid LAVA = Fluids.LAVA;

    /** Forge compat: look up a fluid by its registry name. */
    @Nullable
    public static Fluid getFluid(String name) {
        if (name == null || name.isEmpty()) return null;
        Identifier id = name.contains(":") ? new Identifier(name) : new Identifier("minecraft", name);
        Fluid fluid = Registries.FLUID.get(id);
        return fluid == Fluids.EMPTY ? null : fluid;
    }

    /** Forge compat: get the default registry name of a fluid. */
    public static String getFluidName(Fluid fluid) {
        return Registries.FLUID.getId(fluid).toString();
    }

    /** Forge compat: {@code FluidRegistry.getDefaultFluidName(fluid)}. */
    public static String getDefaultFluidName(Fluid fluid) {
        return getFluidName(fluid);
    }

    /** Forge compat: returns all registered fluids keyed by name. */
    public static Map<String, Fluid> getRegisteredFluids() {
        Map<String, Fluid> map = new HashMap<>();
        for (Fluid fluid : Registries.FLUID) {
            if (fluid != Fluids.EMPTY) {
                map.put(Registries.FLUID.getId(fluid).toString(), fluid);
            }
        }
        return map;
    }

    /** Forge compat: {@code FluidRegistry.enableUniversalBucket()} — no-op on Fabric. */
    public static void enableUniversalBucket() {
        // no-op — Fabric uses the vanilla bucket system
    }
}
