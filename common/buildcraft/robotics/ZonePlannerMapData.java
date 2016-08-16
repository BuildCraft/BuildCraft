package buildcraft.robotics;

import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public abstract class ZonePlannerMapData {
    private Map<ZonePlannerMapChunkKey, ZonePlannerMapChunk> data = new HashMap<>();

    public abstract void loadChunk(World world, ZonePlannerMapChunkKey zonePlannerMapChunkKey, Consumer<ZonePlannerMapChunk> callback);

    public void getChunk(World world, ZonePlannerMapChunkKey zonePlannerMapChunkKey, Consumer<ZonePlannerMapChunk> callback) {
        if(getLoadedChunk(zonePlannerMapChunkKey) != null) {
            callback.accept(getLoadedChunk(zonePlannerMapChunkKey));
        } else {
            loadChunk(world, zonePlannerMapChunkKey, zonePlannerMapChunk -> {
                data.put(zonePlannerMapChunkKey, zonePlannerMapChunk);
                callback.accept(zonePlannerMapChunk);
            });
        }
    }

    public ZonePlannerMapChunk getLoadedChunk(ZonePlannerMapChunkKey zonePlannerMapChunkKey) {
        return data.get(zonePlannerMapChunkKey);
    }
}