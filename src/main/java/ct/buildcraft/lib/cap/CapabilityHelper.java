/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.lib.cap;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import ct.buildcraft.api.core.BCLog;
import ct.buildcraft.api.core.EnumPipePart;

import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.NonNullSupplier;

/** Provides a simple way of mapping {@link Capability}'s to instances. Also allows for additional providers */
public class CapabilityHelper implements ICapabilityProvider {
    private final Map<EnumPipePart, Map<Capability<?>, NonNullSupplier<?>>> caps = new EnumMap<>(EnumPipePart.class);
    private final List<ICapabilityProvider> additional = new ArrayList<>();

    public CapabilityHelper() {
        for (EnumPipePart face : EnumPipePart.VALUES) {
            caps.put(face, new HashMap<>());
        }
    }

    private Map<Capability<?>, NonNullSupplier<?>> getCapMap(Direction facing) {
        return caps.get(EnumPipePart.fromFacing(facing));
    }

    public <T> void addCapabilityInstance(@Nullable Capability<T> cap, T instance, EnumPipePart... parts) {
    	NonNullSupplier<T> supplier = () -> instance;
        addCapability(cap, supplier, parts);
    }

    public <T> void addCapability(@Nullable Capability<T> cap, NonNullSupplier<T> getter, EnumPipePart... parts) {
        if (cap == null) {
            return;
        }
        for (EnumPipePart part : parts) {
            caps.get(part).put(cap, getter);
        }
    }

    public <T> void addCapability(@Nullable Capability<T> cap, Function<Direction, T> getter, EnumPipePart... parts) {
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
    public <T> @NotNull LazyOptional<T> getCapability(@Nonnull Capability<T> capability, Direction facing) {
        Map<Capability<?>, NonNullSupplier<?>> capMap = getCapMap(facing);
        NonNullSupplier<?> supplier = capMap.get(capability);
        if (supplier != null) {
            return LazyOptional.of(supplier).cast();
        }
        for (ICapabilityProvider provider : additional) {
        	var a = provider.getCapability(capability, facing);
        	if(a == null) {
        		BCLog.logger.debug("aaaaa");
        	}
            if (provider.getCapability(capability, facing).isPresent()) {
                return provider.getCapability(capability, facing);
            }
        }
        return LazyOptional.empty();
    }
}
