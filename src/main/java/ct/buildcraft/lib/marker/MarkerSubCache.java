/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.lib.marker;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ct.buildcraft.api.core.BCDebugging;
import ct.buildcraft.api.core.BCLog;
import ct.buildcraft.lib.block.BlockMarkerBase;
import ct.buildcraft.lib.client.render.laser.LaserData_BC8.LaserType;
import ct.buildcraft.lib.net.MessageManager;
import ct.buildcraft.lib.net.MessageMarker;
import ct.buildcraft.lib.tile.TileMarker;
import com.google.common.collect.ImmutableList;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class MarkerSubCache<C extends MarkerConnection<C>> {
    public static final boolean DEBUG_FULL = BCDebugging.shouldDebugComplex("lib.marker.full");

    public final int cacheId;
    public final ResourceKey<Level> dimensionId;
    public final boolean isServer;
    private final Level level;
    private final Map<BlockPos, C> posToConnection = new ConcurrentHashMap<>();
    private final Map<C, Set<BlockPos>> connectionToPos = new ConcurrentHashMap<>();
    private final Map<BlockPos, Optional<TileMarker<C>>> tileCache = new ConcurrentHashMap<>();

    private boolean isDirty = false;

    public MarkerSubCache(Level world, int cacheId) {
        this.level = world;
        this.isServer = !world.isClientSide;
        this.dimensionId = world.dimension();
        this.cacheId = cacheId;
    }

    public void onPlayerJoinLevel(ServerPlayer player) {
        if (isServer) {// Sanity Check
            validateLoadedMarkers();
            // Send ALL loaded markers
            if (!tileCache.isEmpty()) {
                MessageMarker message = new MessageMarker();
                message.add = true;
                message.connection = false;
                message.cacheId = cacheId;
                message.positions.addAll(tileCache.keySet());
                MessageManager.sendTo(message, player);
            }
            // Send ALL connections.
            for (C connection : connectionToPos.keySet()) {
                MessageMarker message = new MessageMarker();
                message.add = true;
                message.connection = true;
                message.cacheId = cacheId;
                message.positions.addAll(connection.getMarkerPositions());
                MessageManager.sendTo(message, player);
            }
        }
    }

    public boolean hasLoadedOrUnloadedMarker(BlockPos pos) {
        validateLoadedMarker(pos);
        return tileCache.containsKey(pos);
    }

    @Nullable
    public TileMarker<C> getMarker(BlockPos pos) {
        validateLoadedMarker(pos);
        Optional<TileMarker<C>> op = tileCache.get(pos);
        if (op == null) {
            return null;
        } else {
            return op.orElse(null);
        }
    }

    public void loadMarker(BlockPos pos, @Nullable TileMarker<C> marker) {
        setDirty(true);
        Optional<TileMarker<C>> previous = tileCache.get(pos);
        boolean did = previous != null;
        boolean wasUnloaded = did && previous.isEmpty();
        tileCache.put(pos, Optional.ofNullable(marker));
        if (DEBUG_FULL) {
            BCLog.logger.info("[lib.marker.full] Set a marker at " + pos + " as " + marker);
        }
        if (isServer && (!did || wasUnloaded || marker != null)) {
            sendMarkerAddedToDimension(pos);
            C connection = posToConnection.get(pos);
            if (connection != null) {
                sendConnectionAddedToDimension(connection);
            }
        }
    }

    public void unloadMarker(BlockPos pos) {
        // Keep the marker position in the cache while the chunk is unloaded so existing marker boxes stay valid.
        // Vanilla calls setRemoved() for chunk unloads, so this must not behave like a real marker removal and it
        // must not clear unrelated dirty state in the saved marker data.
        boolean wasDirty = isDirty;
        tileCache.put(pos, Optional.empty());
        setDirty(wasDirty);
        if (DEBUG_FULL) {
            BCLog.logger.info("[lib.marker.full] Unloaded marker at " + pos);
        }
    }

    public void removeMarker(BlockPos pos) {
        setDirty(true);
        if (DEBUG_FULL) {
            BCLog.logger.info("[lib.marker.full] Removed a marker at " + pos);
        }
        tileCache.remove(pos);
        C connection = posToConnection.get(pos);
        if (connection != null) {
            connection.removeMarker(pos);
            refreshConnection(connection);
        }
        if (isServer) {
            sendMarkerRemovedToDimension(pos);
        }
    }

    public void syncMarkerState(BlockPos pos) {
        if (!isServer) {
            return;
        }
        if (tileCache.containsKey(pos)) {
            sendMarkerAddedToDimension(pos);
        }
        C connection = getConnection(pos);
        if (connection != null) {
            sendConnectionAddedToDimension(connection);
        }
    }

    private void sendMarkerAddedToDimension(BlockPos pos) {
        MessageMarker message = new MessageMarker();
        message.add = true;
        message.connection = false;
        message.multiple = false;
        message.cacheId = cacheId;
        message.count = 1;
        message.positions.add(pos);
        MessageManager.sendToDimension(message, dimensionId);
    }

    private void sendMarkerRemovedToDimension(BlockPos pos) {
        MessageMarker message = new MessageMarker();
        message.add = false;
        message.connection = false;
        message.multiple = false;
        message.cacheId = cacheId;
        message.count = 1;
        message.positions.add(pos);
        MessageManager.sendToDimension(message, dimensionId);
    }

    private void sendConnectionAddedToDimension(C connection) {
        MessageMarker message = new MessageMarker();
        message.add = true;
        message.connection = true;
        message.cacheId = cacheId;
        message.positions.addAll(connection.getMarkerPositions());
        message.count = message.positions.size();
        message.multiple = message.count > 1;
        MessageManager.sendToDimension(message, dimensionId);
    }

    private void sendConnectionRemovedToDimension(Set<BlockPos> positions) {
        MessageMarker message = new MessageMarker();
        message.add = false;
        message.connection = true;
        message.cacheId = cacheId;
        message.positions.addAll(positions);
        message.count = message.positions.size();
        message.multiple = message.count > 1;
        MessageManager.sendToDimension(message, dimensionId);
    }

    public ImmutableList<BlockPos> getAllMarkers() {
        validateLoadedMarkers();
        return ImmutableList.copyOf(tileCache.keySet());
    }

    @Nullable
    public C getConnection(BlockPos pos) {
        validateLoadedMarker(pos);
        return posToConnection.get(pos);
    }

    public void destroyConnection(@Nullable C connection) {
        setDirty(true);
        if (connection == null) {
            return;
        }
        Set<BlockPos> set = connectionToPos.remove(connection);
        if (set != null) {
            deinitConnection(set);
        }

        if (DEBUG_FULL) {
            validateAllConnections();
        }
    }

    public void addConnection(@Nonnull C connection) {
        setDirty(true);
        Set<BlockPos> lastSeen = new HashSet<>(connection.getMarkerPositions());
        initConnection(connection, lastSeen);
        if (DEBUG_FULL) {
            validateAllConnections();
        }
    }

    public void refreshConnection(@Nonnull C connection) {
        setDirty(true);
        Set<BlockPos> lastSeen = connectionToPos.get(connection);
        if (DEBUG_FULL) {
            BCLog.logger.info("[lib.marker.full] Refreshing a connection");
            BCLog.logger.info("[lib.marker.full]    - Old = " + lastSeen);
            BCLog.logger.info("[lib.marker.full]    - New = " + connection.getMarkerPositions());
        }
        if (lastSeen == null) {
            // Why did you call this?
            addConnection(connection);
        } else {
            Set<BlockPos> invalid = new HashSet<>(lastSeen);
            lastSeen = new HashSet<>(connection.getMarkerPositions());
            invalid.removeAll(lastSeen);
            deinitConnection(invalid);
            initConnection(connection, lastSeen);
            if (lastSeen.isEmpty()) {
                connectionToPos.remove(connection);
            }
        }

        if (DEBUG_FULL) {
            validateAllConnections();
        }
    }

    private void validateAllConnections() {
        setDirty(true);
        final String logStart = "[lib.marker.full][" + cacheId + "]";

        Set<C> visited = new HashSet<>();
        Set<BlockPos> visitedPos = new HashSet<>();

        for (Entry<C, Set<BlockPos>> entry : connectionToPos.entrySet()) {
            C con = entry.getKey();
            Set<BlockPos> positions = entry.getValue();
            Set<BlockPos> actual = new HashSet<>(con.getMarkerPositions());
            if (!positions.equals(actual)) {
                BCLog.logger.warn(logStart + " Positions differed!");
                List<BlockPos> total = new ArrayList<>();
                total.addAll(positions);
                total.addAll(actual);
                for (BlockPos p : total) {
                    String s = "(";
                    s += positions.contains(p) ? "R" : "_";
                    s += actual.contains(p) ? "S" : "_";
                    BCLog.logger.warn(logStart + "  - " + p + " " + s + ")");
                }
            }
            for (BlockPos p : positions) {
                if (visitedPos.contains(p)) {
                    BCLog.logger.warn(logStart + " Duplicate block positions!" + p + " - " + con);
                }
                visitedPos.add(p);
            }
            visited.add(con);
        }

        for (Entry<BlockPos, C> entry : posToConnection.entrySet()) {
            C connection = entry.getValue();
            BlockPos p = entry.getKey();
            if (!visited.contains(connection)) {
                BCLog.logger.warn(logStart + " Unknown connection " + connection + "(" + p + ")");
            }
            if (!visitedPos.contains(p)) {
                BCLog.logger.warn(logStart + " Unknown Position " + p + " (" + connection + ")");
            }
        }
    }

    private void deinitConnection(Set<BlockPos> set) {
        setDirty(true);
        if (DEBUG_FULL) {
            BCLog.logger.info("[lib.marker.full] Tearing down all connections in " + set);
        }
        for (BlockPos p : set) {
            posToConnection.remove(p);
        }
        if (isServer && set.size() > 0) {
            sendConnectionRemovedToDimension(set);
        }
    }

    private void initConnection(C connection, Set<BlockPos> lastSeen) {
        setDirty(true);
        if (DEBUG_FULL) {
            BCLog.logger.info("[lib.marker.full] Setting up a connection with " + lastSeen);
        }
        if (lastSeen.size() < 2) {
            connectionToPos.remove(connection);
            for (BlockPos p : lastSeen) {
                posToConnection.remove(p);
            }
            return;
        }

        connectionToPos.put(connection, lastSeen);
        for (BlockPos p : lastSeen) {
            posToConnection.put(p, connection);
        }
        if (isServer && lastSeen.size() > 0) {
            sendConnectionAddedToDimension(connection);
        }
    }

    public ImmutableList<C> getConnections() {
        validateLoadedMarkers();
        ImmutableList.Builder<C> builder = ImmutableList.builder();
        for (Entry<C, Set<BlockPos>> entry : connectionToPos.entrySet()) {
            Set<BlockPos> positions = entry.getValue();
            if (positions != null && positions.size() >= 2) {
                builder.add(entry.getKey());
            }
        }
        return builder.build();
    }

    private void validateLoadedMarkers() {
        Set<BlockPos> toCheck = new HashSet<>();
        toCheck.addAll(tileCache.keySet());
        toCheck.addAll(posToConnection.keySet());
        for (BlockPos pos : toCheck) {
            validateLoadedMarker(pos);
        }
    }

    private void validateLoadedMarker(BlockPos pos) {
        if (!tileCache.containsKey(pos) && !posToConnection.containsKey(pos)) {
            return;
        }
        if (isMarkerStillValidOrUnloaded(pos)) {
            return;
        }
        if (DEBUG_FULL) {
            BCLog.logger.info("[lib.marker.full] Pruning stale marker at " + pos);
        }
        removeMarker(pos);
    }

    private boolean isMarkerStillValidOrUnloaded(BlockPos pos) {
        if (!level.hasChunkAt(pos)) {
            return true;
        }
        if (!(level.getBlockState(pos).getBlock() instanceof BlockMarkerBase)) {
            return false;
        }
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity == null) {
            return true;
        }
        if (!(blockEntity instanceof TileMarker<?> marker)) {
            return false;
        }
        return ((Object) marker.getCache().getSubCache(level)) == this;
    }

    public abstract boolean tryConnect(BlockPos from, BlockPos to);

    /** Checks if {@link #tryConnect(BlockPos, BlockPos)} would succeed at this time. */
    public abstract boolean canConnect(BlockPos a, BlockPos b);

    public abstract ImmutableList<BlockPos> getValidConnections(BlockPos from);

    @OnlyIn(Dist.CLIENT)
    public abstract LaserType getPossibleLaserType();

    @OnlyIn(Dist.CLIENT)
    public final void handleMessageMain(MessageMarker message) {
        setDirty(true);
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

    @OnlyIn(Dist.CLIENT)
    protected abstract boolean handleMessage(MessageMarker message);

    public boolean isDirty() {
        return isDirty;
    }

    public void setDirty(boolean isDirty) {
        this.isDirty = isDirty;
    }
}
