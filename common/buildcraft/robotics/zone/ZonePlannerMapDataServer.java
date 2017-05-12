package buildcraft.robotics.zone;

import net.minecraft.world.World;

public class ZonePlannerMapDataServer extends ZonePlannerMapData {
    public static final ZonePlannerMapDataServer INSTANCE = new ZonePlannerMapDataServer();

    @Override
    public ZonePlannerMapChunk loadChunk(World world, ZonePlannerMapChunkKey key) {
        return new ZonePlannerMapChunk(world, key);
    }
}
