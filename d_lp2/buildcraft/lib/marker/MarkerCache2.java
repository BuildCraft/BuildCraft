package buildcraft.lib.marker;

import java.util.*;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import buildcraft.lib.marker.MarkerCache2.PerWorld2;
import buildcraft.lib.tile.TileMarker;

public abstract class MarkerCache2<S extends PerWorld2<?, ?>> {
    public final String name;

    private final Map<Integer, S> cacheClient = new HashMap<>();
    private final Map<Integer, S> cacheServer = new HashMap<>();

    public MarkerCache2(String name) {
        this.name = name;
    }

    protected abstract S createSubCache(World world);

    public S getSubCache(World world) {
        Map<Integer, S> cache = world.isRemote ? cacheClient : cacheServer;
        int dimId = world.provider.getDimension();
        Integer key = Integer.valueOf(dimId);
        S sub = cache.get(key);
        if (sub == null) {
            sub = createSubCache(world);
            cache.put(key, sub);
        }
        return sub;
    }

    public static abstract class PerWorld2<C extends MarkerConnection2<C>, T extends TileMarker<C, T>> {
        private final Map<BlockPos, C> posToConnection = new HashMap<>();
        private final Map<C, Set<BlockPos>> connectionToPos = new IdentityHashMap<>();
        private final Map<BlockPos, T> tileCache = new HashMap<>();

        public T getMarker(BlockPos pos) {
            return tileCache.get(pos);
        }

        public void addMarker(BlockPos pos, T marker) {
            tileCache.put(pos, marker);
        }

        public void unloadMarker(BlockPos pos) {
            tileCache.remove(pos);
        }

        public C getConnection(BlockPos pos) {
            return posToConnection.get(pos);
        }

        public void destroyConnection(C connection) {
            Set<BlockPos> set = connectionToPos.remove(connection);
            if (set != null) {
                for (BlockPos p : set) {
                    posToConnection.remove(p);
                }
            }
        }

        public void addConnection(C connection) {
            Set<BlockPos> lastSeen = new HashSet<>(connection.getMarkerPositions());
            connectionToPos.put(connection, lastSeen);
            for (BlockPos p : lastSeen) {
                posToConnection.put(p, connection);
            }
        }
    }
}
