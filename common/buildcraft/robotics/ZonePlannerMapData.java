package buildcraft.robotics;

import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public abstract class ZonePlannerMapData {
    private Map<ChunkPos, ZonePlannerMapChunk> data = new HashMap<>();

    public abstract void loadChunk(World world, ChunkPos chunkPos, Consumer<ZonePlannerMapChunk> callback);

    public void getChunk(World world, ChunkPos chunkPos, Consumer<ZonePlannerMapChunk> callback) {
        if(data.containsKey(chunkPos)) {
            callback.accept(data.get(chunkPos));
        } else {
            loadChunk(world, chunkPos, zonePlannerMapChunk -> {
                data.put(chunkPos, zonePlannerMapChunk);
                callback.accept(zonePlannerMapChunk);
            });
        }
    }

    public ZonePlannerMapChunk getLoadedChunk(ChunkPos chunkPos) {
        if(data.containsKey(chunkPos)) {
            return data.get(chunkPos);
        }
        return null;
    }
}