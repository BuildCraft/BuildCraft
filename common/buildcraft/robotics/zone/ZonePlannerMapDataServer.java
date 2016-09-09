package buildcraft.robotics.zone;

import java.util.function.Consumer;

import net.minecraft.world.World;

public class ZonePlannerMapDataServer extends ZonePlannerMapData {
    public static ZonePlannerMapDataServer instance = new ZonePlannerMapDataServer();

    @Override
    public void loadChunk(World world, ZonePlannerMapChunkKey key, Consumer<ZonePlannerMapChunk> callback) {
        callback.accept(new ZonePlannerMapChunk(world, key));
    }
}
