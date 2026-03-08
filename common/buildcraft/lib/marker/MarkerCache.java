/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.marker;

import buildcraft.api.core.BCDebugging;
import buildcraft.api.core.BCLog;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.ModLoadingStage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class MarkerCache<S extends MarkerSubCache<?>> {
    public static final boolean DEBUG = BCDebugging.shouldDebugLog("lib.markers");
    public static final List<MarkerCache<?>> CACHES = new ArrayList<>();

    public final String name;

    private final Map<String, S> cacheClient = new ConcurrentHashMap<>();
    private final Map<String, S> cacheServer = new ConcurrentHashMap<>();

    public MarkerCache(String name) {
        this.name = name;
    }

    public static void registerCache(MarkerCache<?> cache) {
//        if (Loader.instance().hasReachedState(LoaderState.POSTINITIALIZATION))
        if (ModLoadingContext.get().getActiveContainer().getCurrentState().ordinal() >= ModLoadingStage.COMPLETE.ordinal()) {
            throw new IllegalStateException("Registered too late!");
        }
        ModContainer mod = ModLoadingContext.get().getActiveContainer();
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

    // public static void onPlayerJoinWorld(EntityPlayerMP player)
    public static void onPlayerJoinWorld(ServerPlayer player) {
        for (MarkerCache<?> cache : CACHES) {
            Level world = player.level;
            cache.getSubCache(world).onPlayerJoinWorld(player);
        }
    }

    public static void onWorldUnload(Level world) {
        for (MarkerCache<?> cache : CACHES) {
            cache.onWorldUnloadImpl(world);
        }
    }

    private void onWorldUnloadImpl(Level world) {
        Map<String, S> cache = world.isClientSide ? cacheClient : cacheServer;
        String key = world.dimension().location().getPath();
        cache.remove(key);
    }

    protected abstract S createSubCache(Level world);

    public S getSubCache(Level world) {
        Map<String, S> cache = world.isClientSide ? cacheClient : cacheServer;
        String key = world.dimension().location().getPath();
        return cache.computeIfAbsent(key, k -> createSubCache(world));
    }
}
