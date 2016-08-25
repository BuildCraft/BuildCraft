package buildcraft.robotics.zone;

import net.minecraft.world.World;

import java.util.function.Consumer;

public class ZonePlannerMapDataServer extends ZonePlannerMapData {
    public static ZonePlannerMapDataServer instance = new ZonePlannerMapDataServer();

    @Override
    public void loadChunk(World world, ZonePlannerMapChunkKey key, Consumer<ZonePlannerMapChunk> callback) {
        callback.accept(new ZonePlannerMapChunk(world, key));
    }
}
