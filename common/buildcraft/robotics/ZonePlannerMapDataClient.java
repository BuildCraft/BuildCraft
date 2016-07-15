package buildcraft.robotics;

import buildcraft.lib.BCMessageHandler;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ZonePlannerMapDataClient extends ZonePlannerMapData {
    public static ZonePlannerMapDataClient instance = new ZonePlannerMapDataClient();
    public List<Pair<Pair<Integer, Integer>, Consumer<ZonePlannerMapChunk>>> pendingRequests = new ArrayList<>();

    @Override
    public void loadChunk(World world, int chunkX, int chunkZ, Consumer<ZonePlannerMapChunk> callback) {
        Pair<Integer, Integer> chunkPosPair = Pair.of(chunkX, chunkZ);
        Pair<Pair<Integer, Integer>, Consumer<ZonePlannerMapChunk>> pendingRequest;
        Pair[] pendingRequestsArray = new Pair[]{null};
        pendingRequest = Pair.of(chunkPosPair, zonePlannerMapChunk -> {
            pendingRequests.remove(pendingRequestsArray[0]);
            callback.accept(zonePlannerMapChunk);
        });
        pendingRequestsArray[0] = pendingRequest;
        pendingRequests.add(pendingRequest);
        BCMessageHandler.netWrapper.sendToServer(new MessageZonePlannerMapChunkRequest(chunkX, chunkZ));
    }
}
