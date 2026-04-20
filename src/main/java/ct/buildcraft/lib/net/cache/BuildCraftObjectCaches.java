/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.lib.net.cache;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

import ct.buildcraft.api.core.BCLog;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

/** Stores default caches for {@link ItemStack} and {@link FluidStack}. Note that because {@link ItemStack} doesn't
 * override {@link #hashCode()} or {@link #equals(Object)} {@link ItemStackKey} is used as the key type instead, so you
 * probably want to use {@link #storeItemStack(ItemStack)}, {@link #getItemStackId(ItemStack)} and
 * {@link #retrieveItemStack(int)} instead of {@link #CACHE_ITEMS} directly. This also stores the */
public class BuildCraftObjectCaches {
    public static final NetworkedItemStackCache CACHE_ITEMS = new NetworkedItemStackCache();
    public static final NetworkedFluidStackCache CACHE_FLUIDS = new NetworkedFluidStackCache();

    static final List<NetworkedObjectCache<?>> CACHES = new ArrayList<>();

    public static void registerCache(NetworkedObjectCache<?> cache) {
 /*       if (ModLoader.isLoadingStateValid()) {
            throw new IllegalStateException("May only construct a cache BEFORE post-init!");
        }*/
        BuildCraftObjectCaches.CACHES.add(cache);
    }

    /** @see NetworkedObjectCache.ServerView#store(Object) */
    public static int storeItemStack(@Nonnull ItemStack stack) {
        return CACHE_ITEMS.server().store(stack);
    }

    /** @see NetworkedObjectCache.ServerView#getId(Object) */
    public static int getItemStackId(@Nonnull ItemStack stack) {
        return CACHE_ITEMS.server().getId(stack);
    }

    /** @see NetworkedObjectCache.ClientView#retrieve(int) */
    public static Supplier<ItemStack> retrieveItemStack(int id) {
        return CACHE_ITEMS.client().retrieve(id);
    }

    /** Called by BuildCraftLib in the {@link FMLPreInitializationEvent} */
    public static void fmlPreInit() {
        registerCache(CACHE_ITEMS);
        registerCache(CACHE_FLUIDS);
    }

    /** Called by BuildCraftLib in the {@link FMLPostInitializationEvent} */
    public static void fmlPostInit() {
        CACHES.sort(Comparator.comparing(a -> a.getClass().getSimpleName()));
        if (NetworkedObjectCache.DEBUG_LOG) {
            BCLog.logger.info("[lib.net.cache] Sorted list of networked object caches:");
            for (int i = 0; i < CACHES.size(); i++) {
                final NetworkedObjectCache<?> cache = CACHES.get(i);
                BCLog.logger.info("  " + i + " = " + cache.getCacheName());
            }
            BCLog.logger.info("[lib.net.cache] Total of " + CACHES.size() + " caches");
        }
    }

    /** Called by BuildCraftLib on every client tick. */
    public static void onClientTick() {
        for (NetworkedObjectCache<?> cache : CACHES) {
            cache.onClientWorldTick();
        }
    }

    /** Called by BuildCraftLib on the client side whenever it joins a server. */
    public static void onClientJoinServer() {
        for (NetworkedObjectCache<?> cache : CACHES) {
            cache.onClientJoinServer();
        }
    }
}
