/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.lib.misc;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import ct.buildcraft.api.inventory.IItemTransactor;

import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;

/** Provides various @Nonnull static final fields storing various capabilities. */
public class CapUtil {
    @Nonnull
    public static final Capability<IItemHandler> CAP_ITEMS;

    @Nonnull
    public static final Capability<IFluidHandler> CAP_FLUIDS;

    @Nonnull
    public static final Capability<IItemTransactor> CAP_ITEM_TRANSACTOR;


    static {
/*        if (!Loader.instance().hasReachedState(LoaderState.INITIALIZATION)) {
            throw new IllegalStateException("Used CapUtil too early, you must wait until init or later!");
        }*/

        CAP_ITEMS = getCapNonNull(ForgeCapabilities.ITEM_HANDLER, IItemHandler.class);
        CAP_FLUIDS = getCapNonNull(ForgeCapabilities.FLUID_HANDLER, IFluidHandler.class);
        CAP_ITEM_TRANSACTOR = CapabilityManager.get(new CapabilityToken<>(){});
        // FIXME: Move cap registration into API!

    }

    @Nonnull
    private static <T> Capability<T> getCapNonNull(Capability<T> cap, Class<T> clazz) {
        if (cap == null) {
            throw new NullPointerException("The capability " + clazz + " was null!");
        }
        return cap;
    }

    /** Attempts to fetch the given capability from the given provider, or returns null if either of those two are
     * null. */
    @Nullable
    public static <T> @NotNull LazyOptional<T> getCapability(ICapabilityProvider provider, Capability<T> capability, Direction facing) {
        if (provider == null || capability == null) {
            return null;
        }
        return provider.getCapability(capability, facing);
    }
}
