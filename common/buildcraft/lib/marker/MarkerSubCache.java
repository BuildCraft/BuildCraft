package buildcraft.lib.marker;

import java.util.*;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.lib.BCMessageHandler;
import buildcraft.lib.client.render.laser.LaserData_BC8.LaserType;
import buildcraft.lib.misc.MessageUtil;
import buildcraft.lib.net.MessageMarker;
import buildcraft.lib.tile.TileMarker;

public abstract class MarkerSubCache<C extends MarkerConnection<C>> {
    public final int cacheId;
    public final int dimensionId;
    public final boolean isServer;
    private final Map<BlockPos, C> posToConnection = new HashMap<>();
    private final Map<C, Set<BlockPos>> connectionToPos = new IdentityHashMap<>();
    private final Map<BlockPos, TileMarker<C>> tileCache = new HashMap<>();

    public MarkerSubCache(World world, int cacheId) {
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
                BCMessageHandler.netWrapper.sendTo(message, player);
            }
            // Send ALL connections.
            for (C connection : connectionToPos.keySet()) {
                MessageMarker message = new MessageMarker();
                message.add = true;
                message.connection = true;
                message.cacheId = cacheId;
                message.positions.addAll(connection.getMarkerPositions());
                BCMessageHandler.netWrapper.sendTo(message, player);
            }
        }
    }
    
    public boolean hasLoadedOrUnloadedMarker(BlockPos pos) {
        return tileCache.containsKey(pos);
    }

    @Nullable
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

    public void removeConnection(BlockPos pos1, BlockPos pos2) {
        for(MarkerConnection connection : getConnections()) {
            BlockPos[] positions = (BlockPos[]) connection.getMarkerPositions().toArray(new BlockPos[0]);
            if(Arrays.binarySearch(positions, pos1) >= 0 && Arrays.binarySearch(positions, pos2) >= 0) {
                deinitConnection(new HashSet<>(Arrays.asList(positions)));
            }
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
        for(MarkerConnection<C> currentConnection : getConnections()) {
            if(currentConnection.getMarkerPositions().containsAll(connection.getMarkerPositions())) {
                deinitConnection(new HashSet<>(currentConnection.getMarkerPositions()));
            }
        }
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
