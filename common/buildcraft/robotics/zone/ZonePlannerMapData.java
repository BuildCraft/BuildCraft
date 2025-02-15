/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.robotics.zone;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.minecraft.world.level.Level;

import java.util.concurrent.TimeUnit;

public abstract class ZonePlannerMapData {
    protected final Cache<ZonePlannerMapChunkKey, ZonePlannerMapChunk> data = CacheBuilder.newBuilder()
            .expireAfterWrite(2, TimeUnit.MINUTES)
            .build();

    /** Use {@link #getChunk(Level, ZonePlannerMapChunkKey)} for a cached version */
    protected abstract ZonePlannerMapChunk loadChunk(Level world, ZonePlannerMapChunkKey key);

    public final ZonePlannerMapChunk getChunk(Level world, ZonePlannerMapChunkKey key) {
        if (data.getIfPresent(key) != null) {
            return data.getIfPresent(key);
        } else {
            ZonePlannerMapChunk zonePlannerMapChunk = loadChunk(world, key);
            if (zonePlannerMapChunk != null) {
                data.put(key, zonePlannerMapChunk);
                return zonePlannerMapChunk;
            }
        }
        return null;
    }
}
