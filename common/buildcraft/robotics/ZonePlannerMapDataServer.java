package buildcraft.robotics;

import net.minecraft.world.World;

import java.util.function.Consumer;

public class ZonePlannerMapDataServer extends ZonePlannerMapData {
    public static ZonePlannerMapDataServer instance = new ZonePlannerMapDataServer();

    @Override
    public void loadChunk(World world, int chunkX, int chunkZ, Consumer<ZonePlannerMapChunk> callback) {
        callback.accept(new ZonePlannerMapChunk().load(world, chunkX, chunkZ));
    }
}
