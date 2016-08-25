package buildcraft.robotics.zone;

import buildcraft.lib.CachedMap;
import net.minecraft.world.World;

import java.util.Map;
import java.util.function.Consumer;

public abstract class ZonePlannerMapData {
    public static final int TIMEOUT = 10 * 60 * 1000;

    private Map<ZonePlannerMapChunkKey, ZonePlannerMapChunk> data = new CachedMap<>(TIMEOUT);

    public abstract void loadChunk(World world, ZonePlannerMapChunkKey zonePlannerMapChunkKey, Consumer<ZonePlannerMapChunk> callback);

    public void getChunk(World world, ZonePlannerMapChunkKey key, Consumer<ZonePlannerMapChunk> callback) {
        if(getLoadedChunk(key) != null) {
            callback.accept(getLoadedChunk(key));
        } else {
            loadChunk(world, key, zonePlannerMapChunk -> {
                data.put(key, zonePlannerMapChunk);
                callback.accept(zonePlannerMapChunk);
            });
        }
    }

    public ZonePlannerMapChunk getLoadedChunk(ZonePlannerMapChunkKey zonePlannerMapChunkKey) {
        return data.get(zonePlannerMapChunkKey);
    }
}