package buildcraft.lib.tile;

import java.util.*;

import com.google.common.collect.ImmutableSet;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;

import net.minecraftforge.common.util.Constants;

@Deprecated
public class MarkerCache<T extends TileMarkerBase<T, C>, C extends MarkerConnection<T, C>> {
    public static final List<MarkerCache<?, ?>> CACHES = new ArrayList<>();

    public final String name;
    public final C connectionLoader;
    private final Map<Integer, PerWorld> cacheServer = new HashMap<>();
    private final Map<Integer, PerWorld> cacheClient = new HashMap<>();

    public MarkerCache(String name, C connectionLoader) {
        this.name = name;
        this.connectionLoader = connectionLoader;
        CACHES.add(this);
    }

    private Map<Integer, PerWorld> getCachePre(World world) {
        return world.isRemote ? cacheClient : cacheServer;
    }

    public PerWorld getCache(World world) {
        Map<Integer, PerWorld> cache = getCachePre(world);
        Integer dim = Integer.valueOf(world.provider.getDimension());
        PerWorld map = cache.get(dim);
        if (map == null) {
            map = new PerWorld(world);
            cache.put(dim, map);
        }
        return map;
    }

    @Deprecated
    public class PerWorld {
        public final Map<BlockPos, T> tiles = new HashMap<>();
        private final Map<BlockPos, C> posToConnection = new HashMap<>();
        private final Map<C, Set<BlockPos>> connections = new IdentityHashMap<>();

        public PerWorld(World world) {
            if (!world.isRemote) {
                WorldSavedData data = world.loadItemData(SavedData.class, SavedData.DATA_ID + name);
                if (data instanceof SavedData) {
                    SavedData save = ((SavedData) data);
                    NBTTagCompound nbt = save.nbt;
                    if (nbt != null) {
                        load(nbt);
                    }
                } else {
                    SavedData save;
                    // Synchronised as world loading might happen at the same time.
                    synchronized (SavedData.DATA_ID) {
                        SavedData.currentWorldId = world.provider.getDimension();
                        save = new SavedData(SavedData.DATA_ID + name);
                        SavedData.currentWorldId = -1;
                    }
                    world.setItemData(save.mapName, save);
                }
            }
        }

        private void load(NBTTagCompound nbt) {
            NBTTagList list = nbt.getTagList("connections", Constants.NBT.TAG_COMPOUND);
            for (int i = 0; i < list.tagCount(); i++) {
                NBTTagCompound sub = list.getCompoundTagAt(i);
                C loaded = connectionLoader.loadFromNBT(this, sub);
                Set<BlockPos> all = new HashSet<>(loaded.getAllMarkers());
                connections.put(loaded, all);
                for (BlockPos p : all) {
                    posToConnection.put(p, loaded);
                }
            }
        }

        public void save(NBTTagCompound nbt) {
            NBTTagList list = new NBTTagList();
            for (C connection : connections.keySet()) {
                list.appendTag(connection.saveToNBT());
            }
            nbt.setTag("connections", list);
        }

        public C getConnection(BlockPos pos) {
            return posToConnection.get(pos);
        }

        public ImmutableSet<C> getAllConnections() {
            return ImmutableSet.copyOf(connections.keySet());
        }

        public void addConnection(C connection) {
            Set<BlockPos> lastSeen = new HashSet<>(connection.getAllMarkers());
            connections.put(connection, lastSeen);
            for (BlockPos p : lastSeen) {
                posToConnection.put(p, connection);
            }
        }

        public void refreshConnection(C connection) {
            if (!connections.containsKey(connection)) {
                throw new IllegalArgumentException("Did not contain the connection!");
            }
            Set<BlockPos> allConnected = new HashSet<>(connection.getAllMarkers());
            if (allConnected.isEmpty()) {
                removeConnection(connection);
            } else {
                Set<BlockPos> invalid = connections.get(connection);
                invalid.removeAll(allConnected);
                for (BlockPos p : invalid) {
                    posToConnection.remove(p);
                }
                for (BlockPos p : allConnected) {
                    posToConnection.put(p, connection);
                }
                connections.put(connection, allConnected);
            }
        }

        public void removeConnection(C connection) {
            Set<BlockPos> invalid = connections.remove(connection);
            if (invalid == null) return;
            for (BlockPos p : invalid) {
                posToConnection.remove(p);
            }
        }
    }

    public static class SavedData extends WorldSavedData {
        public static final String DATA_ID = "bc_marker_connections_";
        public static volatile int currentWorldId = -1;
        public final int dimId;
        public NBTTagCompound nbt;

        public SavedData(String name) {
            super(name);
            this.dimId = currentWorldId;
        }

        @Override
        public void readFromNBT(NBTTagCompound nbt) {
            this.nbt = nbt;
        }

        @Override
        public void writeToNBT(NBTTagCompound nbt) {
            for (MarkerCache<?, ?> cache : CACHES) {
                if (this.mapName.equals(DATA_ID + cache.name)) {
                    MarkerCache<?, ?>.PerWorld perWorld = cache.cacheServer.get(dimId);
                    perWorld.save(nbt);
                    return;
                }
            }
        }

        @Override
        public boolean isDirty() {
            return true;
        }
    }
}
