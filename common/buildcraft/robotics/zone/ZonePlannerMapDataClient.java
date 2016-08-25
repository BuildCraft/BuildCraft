package buildcraft.robotics.zone;

import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Consumer;

import net.minecraft.world.World;

import buildcraft.lib.BCMessageHandler;

public class ZonePlannerMapDataClient extends ZonePlannerMapData {
    public static ZonePlannerMapDataClient instance = new ZonePlannerMapDataClient();
    public final Map<ZonePlannerMapChunkKey, Deque<Consumer<ZonePlannerMapChunk>>> pendingRequests = new ConcurrentHashMap<>();

    @Override
    public void loadChunk(World world, ZonePlannerMapChunkKey key, Consumer<ZonePlannerMapChunk> callback) {
        if (!pendingRequests.containsKey(key)) {
            pendingRequests.put(key, new ConcurrentLinkedDeque<>());
        }
        pendingRequests.get(key).add(callback);
        BCMessageHandler.netWrapper.sendToServer(new MessageZoneMapRequest(key));
    }
}
