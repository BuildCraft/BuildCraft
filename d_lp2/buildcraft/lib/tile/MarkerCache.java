package buildcraft.lib.tile;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class MarkerCache<T extends TileMarkerBase<T>> {
    public final String name;
    private final Map<Integer, Map<BlockPos, T>> cacheServer = new HashMap<>();
    private final Map<Integer, Map<BlockPos, T>> cacheClient = new HashMap<>();

    public MarkerCache(String name) {
        this.name = name;
    }

    private Map<Integer, Map<BlockPos, T>> getCachePre(World world) {
        return world.isRemote ? cacheClient : cacheServer;
    }

    public Map<BlockPos, T> getCache(World world) {
        Map<Integer, Map<BlockPos, T>> cache = world.isRemote ? cacheClient : cacheServer;
        Integer dim = Integer.valueOf(world.provider.getDimension());
        Map<BlockPos, T> map = cache.get(dim);
        if (map == null) {
            map = new HashMap<>();
            cache.put(dim, map);
        }
        return map;
    }
}
