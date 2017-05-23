/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.marker;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;

import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.LoaderState;
import net.minecraftforge.fml.common.ModContainer;

import buildcraft.api.core.BCDebugging;
import buildcraft.api.core.BCLog;

public abstract class MarkerCache<S extends MarkerSubCache<?>> {
    public static final boolean DEBUG = BCDebugging.shouldDebugLog("lib.markers");
    public static final List<MarkerCache<?>> CACHES = new ArrayList<>();

    public final String name;

    private final Map<Integer, S> cacheClient = new ConcurrentHashMap<>();
    private final Map<Integer, S> cacheServer = new ConcurrentHashMap<>();

    public MarkerCache(String name) {
        this.name = name;
    }

    public static void registerCache(MarkerCache<?> cache) {
        if (Loader.instance().hasReachedState(LoaderState.POSTINITIALIZATION)) {
            throw new IllegalStateException("Registered too late!");
        }
        ModContainer mod = Loader.instance().activeModContainer();
        if (mod == null) {
            throw new IllegalStateException("Tried to register a cache without an active mod!");
        }
        CACHES.add(cache);
        if (DEBUG) {
            BCLog.logger.info("[lib.markers] Registered a cache " + cache.name + " with an ID of " + (CACHES.size() - 1) + " from " + mod.getModId());
        }
    }

    public static void postInit() {
        if (DEBUG) {
            BCLog.logger.info("[lib.markers] Sorted list of cache types:");
            for (int i = 0; i < CACHES.size(); i++) {
                final MarkerCache<?> cache = CACHES.get(i);
                BCLog.logger.info("  " + i + " = " + cache.name);
            }
            BCLog.logger.info("[lib.markers] Total of " + CACHES.size() + " cache types");
        }
    }

    public static void onPlayerJoinWorld(EntityPlayerMP player) {
        for (MarkerCache<?> cache : CACHES) {
            World world = player.world;
            cache.getSubCache(world).onPlayerJoinWorld(player);
        }
    }

    public static void onWorldUnload(World world) {
        for (MarkerCache<?> cache : CACHES) {
            cache.onWorldUnloadImpl(world);
        }
    }

    private void onWorldUnloadImpl(World world) {
        Map<Integer, S> cache = world.isRemote ? cacheClient : cacheServer;
        Integer key = world.provider.getDimension();
        cache.remove(key);
    }

    protected abstract S createSubCache(World world);

    public S getSubCache(World world) {
        Map<Integer, S> cache = world.isRemote ? cacheClient : cacheServer;
        Integer key = world.provider.getDimension();
        return cache.computeIfAbsent(key, k -> createSubCache(world));
    }
}
