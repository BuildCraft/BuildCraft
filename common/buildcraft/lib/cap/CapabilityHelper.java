/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.cap;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.util.EnumFacing;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import buildcraft.api.core.EnumPipePart;

/** Provides a simple way of mapping {@link Capability}'s to instances. Also allows for additional providers */
public class CapabilityHelper implements ICapabilityProvider {
    private final Map<EnumPipePart, Map<Capability<?>, Supplier<?>>> caps = new EnumMap<>(EnumPipePart.class);
    private final List<ICapabilityProvider> additional = new ArrayList<>();

    public CapabilityHelper() {
        for (EnumPipePart face : EnumPipePart.VALUES) {
            caps.put(face, new HashMap<>());
        }
    }

    private Map<Capability<?>, Supplier<?>> getCapMap(EnumFacing facing) {
        return caps.get(EnumPipePart.fromFacing(facing));
    }

    public <T> void addCapabilityInstance(@Nullable Capability<T> cap, T instance, EnumPipePart... parts) {
        Supplier<T> supplier = () -> instance;
        addCapability(cap, supplier, parts);
    }

    public <T> void addCapability(@Nullable Capability<T> cap, Supplier<T> getter, EnumPipePart... parts) {
        if (cap == null) {
            return;
        }
        for (EnumPipePart part : parts) {
            caps.get(part).put(cap, getter);
        }
    }

    public <T> void addCapability(@Nullable Capability<T> cap, Function<EnumFacing, T> getter, EnumPipePart... parts) {
        if (cap == null) {
            return;
        }
        for (EnumPipePart part : parts) {
            caps.get(part).put(cap, () -> getter.apply(part.face));
        }
    }

    public <T extends ICapabilityProvider> T addProvider(T provider) {
        if (provider != null) {
            additional.add(provider);
        }
        return provider;
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, EnumFacing facing) {
        return getCapability(capability, facing) != null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, EnumFacing facing) {
        Map<Capability<?>, Supplier<?>> capMap = getCapMap(facing);
        Supplier<?> supplier = capMap.get(capability);
        if (supplier != null) {
            return (T) supplier.get();
        }
        for (ICapabilityProvider provider : additional) {
            if (provider.hasCapability(capability, facing)) {
                return provider.getCapability(capability, facing);
            }
        }
        return null;
    }
}
