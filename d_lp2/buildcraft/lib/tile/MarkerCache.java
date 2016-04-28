package buildcraft.lib.tile;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class MarkerCache<T extends TileMarkerBase<T>> {
    public final String name;
    private final Map<BlockPos, T> cacheServer = new HashMap<>();
    private final Map<BlockPos, T> cacheClient = new HashMap<>();

    public MarkerCache(String name) {
        this.name = name;
    }

    public Map<BlockPos, T> getCache(World world) {
        return world.isRemote ? cacheClient : cacheServer;

    }
}
