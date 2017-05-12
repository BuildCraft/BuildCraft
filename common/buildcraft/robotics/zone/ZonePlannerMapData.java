package buildcraft.robotics.zone;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.minecraft.world.World;

import java.util.concurrent.TimeUnit;

public abstract class ZonePlannerMapData {
    protected final Cache<ZonePlannerMapChunkKey, ZonePlannerMapChunk> data = CacheBuilder.newBuilder()
            .expireAfterWrite(2, TimeUnit.MINUTES)
            .build();

    /** Use {@link #getChunk(World, ZonePlannerMapChunkKey)} for a cached version */
    protected abstract ZonePlannerMapChunk loadChunk(World world, ZonePlannerMapChunkKey key);

    public final ZonePlannerMapChunk getChunk(World world, ZonePlannerMapChunkKey key) {
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
