package buildcraft.lib.marker;

import java.util.*;

import com.google.common.collect.ImmutableList;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.LoaderState;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.core.BCDebugging;
import buildcraft.api.core.BCLog;
import buildcraft.lib.BCMessageHandler;
import buildcraft.lib.client.render.LaserData_BC8.LaserType;
import buildcraft.lib.marker.MarkerCache.SubCache;
import buildcraft.lib.net.MessageMarker;
import buildcraft.lib.tile.TileMarker;

public abstract class MarkerCache<S extends SubCache<?>> {
    public static final boolean DEBUG = BCDebugging.shouldDebugLog("lib.markers");
    public static final List<MarkerCache<?>> CACHES = new ArrayList<>();

    public final String name;

    private final Map<Integer, S> cacheClient = new HashMap<>();
    private final Map<Integer, S> cacheServer = new HashMap<>();

    public MarkerCache(String name) {
        this.name = name;
    }

    public static void registerCache(MarkerCache<?> cache) {
        if (Loader.instance().hasReachedState(LoaderState.POSTINITIALIZATION)) {
            throw new IllegalStateException("Registered too late!");
        }
        ModContainer mod = Loader.instance().activeModContainer();
        if (mod == null) {
            throw new IllegalStateException("Tried to register a cache without an active mod!");
        }
        CACHES.add(cache);
        if (DEBUG) {
            BCLog.logger.info("[lib.markers] Registered a cache " + cache.name + " with an ID of " + (CACHES.size() - 1) + " from " + mod.getModId());
        }
    }

    public static void postInit() {
        if (DEBUG) {
            BCLog.logger.info("[lib.markers] Sorted list of cache types:");
            for (int i = 0; i < CACHES.size(); i++) {
                final MarkerCache<?> cache = CACHES.get(i);
                BCLog.logger.info("  " + i + " = " + cache.name);
            }
            BCLog.logger.info("[lib.markers] Total of " + CACHES.size() + " cache types");
        }
    }

    public static void onPlayerJoinWorld(EntityPlayerMP player) {
        for (MarkerCache<?> cache : CACHES) {
            World world = player.worldObj;
            cache.getSubCache(world).onPlayerJoinWorld(player);
        }
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

    // TODO: World loading and unloading

    public static abstract class SubCache<C extends MarkerConnection<C>> {
        public final int cacheId;
        public final int dimensionId;
        public final boolean isServer;
        private final Map<BlockPos, C> posToConnection = new HashMap<>();
        private final Map<C, Set<BlockPos>> connectionToPos = new IdentityHashMap<>();
        private final Map<BlockPos, TileMarker<C>> tileCache = new HashMap<>();

        public SubCache(World world, int cacheId) {
            this.isServer = !world.isRemote;
            this.dimensionId = world.provider.getDimension();
            this.cacheId = cacheId;
        }

        public void onPlayerJoinWorld(EntityPlayerMP player) {
            if (isServer) {// Sanity Check
                // Send ALL loaded markers
                if (!tileCache.isEmpty()) {
                    MessageMarker message = new MessageMarker();
                    message.add = true;
                    message.connection = false;
                    message.cacheId = cacheId;
                    message.positions.addAll(tileCache.keySet());
                    message.count = message.positions.size();
                    message.multiple = message.count > 1;
                    BCMessageHandler.netWrapper.sendTo(message, player);
                }
                // Send ALL connections.
                for (C connection : connectionToPos.keySet()) {
                    MessageMarker message = new MessageMarker();
                    message.add = true;
                    message.connection = true;
                    message.cacheId = cacheId;
                    message.positions.addAll(connection.getMarkerPositions());
                    message.count = message.positions.size();
                    message.multiple = message.count > 1;
                    BCMessageHandler.netWrapper.sendTo(message, player);
                }
            }
        }

        public boolean hasLoadedOrUnloadedMarker(BlockPos pos) {
            return tileCache.containsKey(pos);
        }

        public TileMarker<C> getMarker(BlockPos pos) {
            return tileCache.get(pos);
        }

        public void loadMarker(BlockPos pos, TileMarker<C> marker) {
            boolean did = tileCache.containsKey(pos);
            tileCache.put(pos, marker);
            if (isServer && !did) {
                MessageMarker message = new MessageMarker();
                message.add = true;
                message.connection = false;
                message.multiple = false;
                message.cacheId = cacheId;
                message.count = 1;
                message.positions.add(pos);
                BCMessageHandler.netWrapper.sendToDimension(message, dimensionId);
            }
        }

        public void unloadMarker(BlockPos pos) {
            loadMarker(pos, null);
        }

        public void removeMarker(BlockPos pos) {
            tileCache.remove(pos);
            C connection = getConnection(pos);
            if (connection != null) {
                connection.removeMarker(pos);
                refreshConnection(connection);
            }
            if (isServer) {
                MessageMarker message = new MessageMarker();
                message.add = false;
                message.connection = false;
                message.multiple = false;
                message.cacheId = cacheId;
                message.count = 1;
                message.positions.add(pos);
                BCMessageHandler.netWrapper.sendToDimension(message, dimensionId);
            }
        }

        public ImmutableList<BlockPos> getAllMarkers() {
            return ImmutableList.copyOf(tileCache.keySet());
        }

        public C getConnection(BlockPos pos) {
            return posToConnection.get(pos);
        }

        public void destroyConnection(C connection) {
            Set<BlockPos> set = connectionToPos.remove(connection);
            if (set != null) {
                deinitConnection(set);
            }
        }

        public void addConnection(C connection) {
            Set<BlockPos> lastSeen = new HashSet<>(connection.getMarkerPositions());
            initConnection(connection, lastSeen);
        }

        public void refreshConnection(C connection) {
            Set<BlockPos> lastSeen = connectionToPos.get(connection);
            if (lastSeen == null) {
                // Why did you call this?
                addConnection(connection);
            } else {
                Set<BlockPos> invalid = new HashSet<>(lastSeen);
                lastSeen = new HashSet<>(connection.getMarkerPositions());
                invalid.removeAll(lastSeen);
                deinitConnection(invalid);
                if (lastSeen.size() > 0) {
                    initConnection(connection, lastSeen);
                }
            }
        }

        private void deinitConnection(Set<BlockPos> set) {
            for (BlockPos p : set) {
                posToConnection.remove(p);
            }
            if (isServer && set.size() > 0) {
                MessageMarker message = new MessageMarker();
                message.add = false;
                message.connection = true;
                message.cacheId = cacheId;
                message.positions.addAll(set);
                message.count = message.positions.size();
                message.multiple = message.count > 1;
                BCMessageHandler.netWrapper.sendToDimension(message, dimensionId);
            }
        }

        private void initConnection(C connection, Set<BlockPos> lastSeen) {
            connectionToPos.put(connection, lastSeen);
            for (BlockPos p : lastSeen) {
                posToConnection.put(p, connection);
            }
            if (isServer && lastSeen.size() > 0) {
                MessageMarker message = new MessageMarker();
                message.add = true;
                message.connection = true;
                message.cacheId = cacheId;
                message.positions.addAll(connection.getMarkerPositions());
                message.count = message.positions.size();
                message.multiple = message.count > 1;
                BCMessageHandler.netWrapper.sendToDimension(message, dimensionId);
            }
        }

        public ImmutableList<C> getConnections() {
            return ImmutableList.copyOf(connectionToPos.keySet());
        }

        public abstract boolean tryConnect(BlockPos from, BlockPos to);

        /** Checks if {@link #tryConnect(BlockPos, BlockPos)} would succeed at this time. */
        public abstract boolean canConnect(BlockPos a, BlockPos b);

        public abstract ImmutableList<BlockPos> getValidConnections(BlockPos from);

        @SideOnly(Side.CLIENT)
        public abstract LaserType getPossibleLaserType();

        @SideOnly(Side.CLIENT)
        public final void handleMessageMain(MessageMarker message) {
            if (handleMessage(message)) {
                return;
            }
            if (!message.connection) {
                List<BlockPos> positions = message.positions;
                if (message.add) {
                    for (BlockPos p : positions) {
                        if (!hasLoadedOrUnloadedMarker(p)) {
                            loadMarker(p, null);
                        }
                    }
                } else {
                    for (BlockPos p : positions) {
                        removeMarker(p);
                    }
                }
            }
        }

        @SideOnly(Side.CLIENT)
        protected abstract boolean handleMessage(MessageMarker message);
    }
}
