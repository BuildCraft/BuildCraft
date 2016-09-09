package buildcraft.robotics.zone;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import net.minecraft.world.World;

public abstract class ZonePlannerMapData {
    public static final int TIMEOUT = 10 * 60 * 1000;

    private final Cache<ZonePlannerMapChunkKey, ZonePlannerMapChunk> data;

    public ZonePlannerMapData() {
        data = CacheBuilder.newBuilder().expireAfterWrite(2, TimeUnit.MINUTES).build();
    }

    public abstract void loadChunk(World world, ZonePlannerMapChunkKey key, Consumer<ZonePlannerMapChunk> onLoad);

    public final void getChunk(World world, ZonePlannerMapChunkKey key, Consumer<ZonePlannerMapChunk> onLoad) {
        ZonePlannerMapChunk loadedChunk = getLoadedChunk(key);
        if (loadedChunk != null) {
            onLoad.accept(loadedChunk);
        } else {
            loadChunk(world, key, chunk -> {
                data.put(key, chunk);
                onLoad.accept(chunk);
            });
        }
    }

    public final ZonePlannerMapChunk getLoadedChunk(ZonePlannerMapChunkKey key) {
        return data.getIfPresent(key);
    }
}
