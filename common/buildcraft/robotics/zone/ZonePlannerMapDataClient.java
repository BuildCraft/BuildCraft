package buildcraft.robotics.zone;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Consumer;

import buildcraft.builders.snapshot.Snapshot;
import net.minecraft.world.World;

import buildcraft.lib.BCMessageHandler;

public class ZonePlannerMapDataClient extends ZonePlannerMapData {
    public static final ZonePlannerMapDataClient INSTANCE = new ZonePlannerMapDataClient();

    private final List<ZonePlannerMapChunkKey> pending = new ArrayList<>();

    @Override
    public ZonePlannerMapChunk loadChunk(World world, ZonePlannerMapChunkKey key) {
        if (!pending.contains(key)) {
            pending.add(key);
            BCMessageHandler.netWrapper.sendToServer(new MessageZoneMapRequest(key));
        }
        return null;
    }


    public void onChunkReceived(ZonePlannerMapChunkKey key, ZonePlannerMapChunk zonePlannerMapChunk) {
        pending.remove(key);
        data.put(key, zonePlannerMapChunk);
    }
}
