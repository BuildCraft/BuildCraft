package buildcraft.robotics;

import buildcraft.lib.BCMessageHandler;
import net.minecraft.world.World;

import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Consumer;

public class ZonePlannerMapDataClient extends ZonePlannerMapData {
    public static ZonePlannerMapDataClient instance = new ZonePlannerMapDataClient();
    public Map<ZonePlannerMapChunkKey, Deque<Consumer<ZonePlannerMapChunk>>> pendingRequests = new ConcurrentHashMap<>();

    @Override
    public void loadChunk(World world, ZonePlannerMapChunkKey zonePlannerMapChunkKey, Consumer<ZonePlannerMapChunk> callback) {
        if(!pendingRequests.containsKey(zonePlannerMapChunkKey)) {
            pendingRequests.put(zonePlannerMapChunkKey, new ConcurrentLinkedDeque<>());
        }
        //noinspection unchecked
        Consumer<ZonePlannerMapChunk>[] localCallbackArray = new Consumer[1];
        localCallbackArray[0] = zonePlannerMapChunk -> {
            pendingRequests.get(zonePlannerMapChunkKey).remove(localCallbackArray[0]);
            callback.accept(zonePlannerMapChunk);
        };
        pendingRequests.get(zonePlannerMapChunkKey).add(localCallbackArray[0]);
        BCMessageHandler.netWrapper.sendToServer(new MessageZonePlannerMapChunkRequest(zonePlannerMapChunkKey));
    }
}
