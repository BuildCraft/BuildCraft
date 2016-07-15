package buildcraft.robotics;

import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public abstract class ZonePlannerMapData {
    private Map<Pair<Integer, Integer>, ZonePlannerMapChunk> data = new HashMap<>();

    public abstract void loadChunk(World world, int chunkX, int chunkZ, Consumer<ZonePlannerMapChunk> callback);

    public void getChunk(World world, int chunkX, int chunkZ, Consumer<ZonePlannerMapChunk> callback) {
        Pair<Integer, Integer> chunkPosPair = Pair.of(chunkX, chunkZ);
        if(data.containsKey(chunkPosPair)) {
            callback.accept(data.get(chunkPosPair));
        } else {
            loadChunk(world, chunkX, chunkZ, zonePlannerMapChunk -> {
                data.put(chunkPosPair, zonePlannerMapChunk);
                callback.accept(zonePlannerMapChunk);
            });
        }
    }
}