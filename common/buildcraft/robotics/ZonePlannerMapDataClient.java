package buildcraft.robotics;

import buildcraft.lib.BCMessageHandler;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Consumer;

public class ZonePlannerMapDataClient extends ZonePlannerMapData {
    public static ZonePlannerMapDataClient instance = new ZonePlannerMapDataClient();
    public Map<Pair<ChunkPos, Integer>, Deque<Consumer<ZonePlannerMapChunk>>> pendingRequests = new ConcurrentHashMap<>();

    @Override
    public void loadChunk(World world, ChunkPos chunkPos, Consumer<ZonePlannerMapChunk> callback) {
        if(!pendingRequests.containsKey(Pair.of(chunkPos, world.provider.getDimension()))) {
            pendingRequests.put(Pair.of(chunkPos, world.provider.getDimension()), new ConcurrentLinkedDeque<>());
        }
        //noinspection unchecked
        Consumer<ZonePlannerMapChunk>[] localCallbackArray = new Consumer[1];
        localCallbackArray[0] = zonePlannerMapChunk -> {
            pendingRequests.get(Pair.of(chunkPos, world.provider.getDimension())).remove(localCallbackArray[0]);
            callback.accept(zonePlannerMapChunk);
        };
        pendingRequests.get(Pair.of(chunkPos, world.provider.getDimension())).add(localCallbackArray[0]);
        BCMessageHandler.netWrapper.sendToServer(new MessageZonePlannerMapChunkRequest(chunkPos));
    }
}
