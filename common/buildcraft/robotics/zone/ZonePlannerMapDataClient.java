package buildcraft.robotics.zone;

import java.util.ArrayList;
import java.util.List;

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
