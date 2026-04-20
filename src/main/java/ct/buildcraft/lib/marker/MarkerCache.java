/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.lib.marker;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import ct.buildcraft.api.core.BCDebugging;
import ct.buildcraft.api.core.BCLog;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.dimension.DimensionType;

public abstract class MarkerCache<S extends MarkerSubCache<?>> {
    public static final boolean DEBUG = BCDebugging.shouldDebugLog("lib.markers");
    public static final List<MarkerCache<?>> CACHES = new ArrayList<>();

    public final String name;

    private final Map<DimensionType, S> cacheClient = new ConcurrentHashMap<>();
    private final Map<DimensionType, S> cacheServer = new ConcurrentHashMap<>();

    public MarkerCache(String name) {
        this.name = name;
    }

    public static void registerCache(MarkerCache<?> cache) {
/*        if (Loader.instance().hasReachedState(LoaderState.POSTINITIALIZATION)) {
            throw new IllegalStateException("Registered too late!");
        }*/
/*        ModContainer mod = FMLLoader..();
        if (mod == null) {
            throw new IllegalStateException("Tried to register a cache without an active mod!");
        }*/
        CACHES.add(cache);
        if (DEBUG) {
            BCLog.logger.info("[lib.markers] Registered a cache " + cache.name + " with an ID of " + (CACHES.size() - 1) + " from "/* + mod.getModId()*/);
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

    public static void onPlayerJoinLevel(ServerPlayer player) {
        for (MarkerCache<?> cache : CACHES) {
        	ServerLevel Level = player.getLevel();
            cache.getSubCache(Level).onPlayerJoinLevel(player);
        }
    }

    public static void onLevelUnload(LevelAccessor levelAccessor) {
        for (MarkerCache<?> cache : CACHES) {
            cache.onLevelUnloadImpl(levelAccessor);
        }
    }

    private void onLevelUnloadImpl(LevelAccessor levelAccessor) {
        Map<DimensionType, S> cache = levelAccessor.isClientSide() ? cacheClient : cacheServer;
        DimensionType key = levelAccessor.dimensionType();
        cache.remove(key);
    }

    protected abstract S createSubCache(Level level);

    public S getSubCache(Level level) {
        Map<DimensionType, S> cache = level.isClientSide ? cacheClient : cacheServer;
        DimensionType key = level.dimensionType();
        return cache.computeIfAbsent(key, k -> createSubCache(level));
    }
}
