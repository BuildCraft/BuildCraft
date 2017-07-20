/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc;

import buildcraft.api.inventory.IItemTransactor;
import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.LoaderState;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import java.util.concurrent.Callable;

/** Provides various @Nonnull static final fields storing various capabilities. */
public class CapUtil {
    @Nonnull
    public static final Capability<IItemHandler> CAP_ITEMS;

    @Nonnull
    public static final Capability<IFluidHandler> CAP_FLUIDS;

    @Nonnull
    public static final Capability<IItemTransactor> CAP_ITEM_TRANSACTOR;

    @CapabilityInject(IItemTransactor.class)
    private static Capability<IItemTransactor> capTransactor;

    static {
        if (!Loader.instance().hasReachedState(LoaderState.INITIALIZATION)) {
            throw new IllegalStateException("Used CapUtil too early, you must wait until init or later!");
        }

        CAP_ITEMS = getCapNonNull(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, IItemHandler.class);
        CAP_FLUIDS = getCapNonNull(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, IFluidHandler.class);

        registerAbstractCapability(IItemTransactor.class);

        // FIXME: Move cap registration into API!
        CAP_ITEM_TRANSACTOR = getCapNonNull(capTransactor, IItemTransactor.class);
    }

    @Nonnull
    private static <T> Capability<T> getCapNonNull(Capability<T> cap, Class<T> clazz) {
        if (cap == null) {
            throw new NullPointerException("The capability " + clazz + " was null!");
        }
        return cap;
    }

    private static <T> void registerAbstractCapability(Class<T> clazz) {
        // By default storing and creating are illegal operations, as we don't necessarily have good default impl's
        IStorage<T> ourStorage = new IStorage<T>() {
            @Override
            public NBTBase writeNBT(Capability<T> capability, T instance, EnumFacing side) {
                throw new IllegalStateException("You must provide your own implementations of " + clazz);
            }

            @Override
            public void readNBT(Capability<T> capability, T instance, EnumFacing side, NBTBase nbt) {
                throw new IllegalStateException("You must provide your own implementations of " + clazz);
            }
        };
        Callable<T> factory = () -> {
            throw new IllegalStateException("You must provide your own instances of " + clazz);
        };
        CapabilityManager.INSTANCE.register(clazz, ourStorage, factory);
    }
}
