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

    public void getChunk(World world, ChunkPos chunkPos, int dimensionalId, Consumer<ZonePlannerMapChunk> callback) {
        if(getLoadedChunk(chunkPos, dimensionalId) != null) {
            callback.accept(getLoadedChunk(chunkPos, dimensionalId));
        } else {
            loadChunk(world, chunkPos, zonePlannerMapChunk -> {
                data.put(chunkPos, zonePlannerMapChunk);
                callback.accept(zonePlannerMapChunk);
            });
        }
    }

    public ZonePlannerMapChunk getLoadedChunk(ChunkPos chunkPos, int dimensionalId) {
        for(Map.Entry<ChunkPos, ZonePlannerMapChunk> chunkPosZonePlannerMapChunkEntry : data.entrySet()) {
            if(chunkPosZonePlannerMapChunkEntry.getKey().equals(chunkPos) && chunkPosZonePlannerMapChunkEntry.getValue().dimensionalId == dimensionalId) {
                return chunkPosZonePlannerMapChunkEntry.getValue();
            }
        }
        return null;
    }
}