/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */
package buildcraft.lib.net;

import java.util.Collection;
import java.util.function.Consumer;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import buildcraft.BuildCraftFabric;

/**
 * Fabric 1.20.1 replacement for the original {@code MessageManager}.
 *
 * Responsibilities:
 *   - Register all BuildCraft packet types on both server and client.
 *   - Provide static {@code sendTo*} helpers whose signatures track the legacy
 *     {@code MessageManager} API closely, so call sites can be ported with
 *     minimal churn.
 *
 * Original (Forge) → Fabric mapping:
 *   SimpleNetworkWrapper              → ServerPlayNetworking / ClientPlayNetworking
 *   IMessage                          → FabricPacket (typed) implementations
 *   IMessageHandler<REQ, REP>         → PlayChannelHandler / PlayPacketHandler
 *   wrapper.sendToServer(msg)         → ClientPlayNetworking.send(packet)
 *   wrapper.sendTo(msg, playerMP)     → ServerPlayNetworking.send(player, packet)
 *   wrapper.sendToAll(msg)            → PlayerLookup.all(server).forEach(send)
 *   wrapper.sendToAllAround(msg, tp)  → PlayerLookup.tracking(world, pos).forEach(send)
 *
 * TODO(R.Chen): once the rest of the lib is migrated, route incoming UpdateTile
 * packets to {@code TileBC_Neptune.receivePayload} on the appropriate side.
 * Currently the receivers just log so the channel is wired but inert.
 */
public final class BCNetworkManager {

    private BCNetworkManager() {}

    /** Call from a {@code ModInitializer.onInitialize()} (common side). */
    public static void registerCommon() {
        BuildCraftFabric.LOGGER.info("[BCNetwork] Registering common packet receivers");

        // Client → Server: tile update from the client (e.g. GUI interactions).
        ServerPlayNetworking.registerGlobalReceiver(
            UpdateTilePayload.TYPE,
            (packet, player, responseSender) -> {
                // STUB(R.Chen): route to TileBC_Neptune.receivePayload on the player's server world.
                // Currently a no-op so the channel is wired but inert.
                BuildCraftFabric.LOGGER.debug(
                    "[BCNetwork] (server) UpdateTile @ {} from {}", packet.vertex(), player.getName().getString()
                );
            }
        );
    }

    /** Call from a {@code ClientModInitializer.onInitializeClient()}. */
    @Environment(EnvType.CLIENT)
    public static void registerClient() {
        BuildCraftFabric.LOGGER.info("[BCNetwork] Registering client packet receivers");

        // Server → Client: tile update broadcast to tracking players.
        ClientPlayNetworking.registerGlobalReceiver(
            UpdateTilePayload.TYPE,
            (packet, player, responseSender) -> {
                // STUB(R.Chen): route to TileBC_Neptune.receivePayload on the client world.
                BuildCraftFabric.LOGGER.debug("[BCNetwork] (client) UpdateTile @ {}", packet.vertex());
            }
        );
    }

    // -----------------------------------------------------------------------
    // Send helpers (kept close to legacy MessageManager signatures)
    // -----------------------------------------------------------------------

    /** Legacy: {@code MessageManager.sendToServer(msg)} — client-only. */
    @Environment(EnvType.CLIENT)
    public static <T extends FabricPacket> void sendToServer(T packet) {
        ClientPlayNetworking.send(packet);
    }

    /** Legacy: {@code MessageManager.sendTo(msg, playerMP)}. */
    public static <T extends FabricPacket> void sendTo(T packet, ServerPlayerEntity player) {
        ServerPlayNetworking.send(player, packet);
    }

    /** Legacy: {@code MessageManager.sendToAll(msg)}. */
    public static <T extends FabricPacket> void sendToAll(T packet, net.minecraft.server.MinecraftServer server) {
        Collection<ServerPlayerEntity> players = PlayerLookup.all(server);
        sendToEach(packet, players);
    }

    /**
     * Legacy: {@code MessageManager.sendToAllAround(msg, NetworkRegistry.TargetPoint)}.
     * Uses Fabric's chunk-tracking lookup: only sends to players that have the
     * chunk containing {@code pos} loaded on their client.
     */
    public static <T extends FabricPacket> void sendToAllTracking(T packet, ServerWorld world, BlockPos pos) {
        sendToEach(packet, PlayerLookup.tracking(world, pos));
    }

    /** Convenience: broadcast to every player tracking the given world's chunk. */
    public static <T extends FabricPacket> void sendToAllInWorld(T packet, World world, BlockPos pos) {
        if (world instanceof ServerWorld serverWorld) {
            sendToAllTracking(packet, serverWorld, pos);
        } else {
            BuildCraftFabric.LOGGER.warn(
                "[BCNetwork] sendToAllInWorld called with non-server world {} — dropped", world
            );
        }
    }

    private static <T extends FabricPacket> void sendToEach(T packet, Iterable<ServerPlayerEntity> players) {
        for (ServerPlayerEntity player : players) {
            ServerPlayNetworking.send(player, packet);
        }
    }

    // -----------------------------------------------------------------------
    // Hook for additional packet registration from other BC modules
    // -----------------------------------------------------------------------

    /**
     * STUB(R.Chen): until module initializers exist, other BC modules can register
     * their packet types by passing a callback here from their own onInitialize().
     * Example:
     *   BCNetworkManager.registerExtra(() -> {
     *       ServerPlayNetworking.registerGlobalReceiver(MyPacket.TYPE, MyHandler::handle);
     *   });
     */
    public static void registerExtra(Runnable r) {
        r.run();
    }

    /** Same as {@link #registerExtra(Runnable)} but lets callers see the active common-side hook. */
    public static <T extends FabricPacket> void registerServerReceiver(
        PacketType<T> type,
        ServerPlayNetworking.PlayPacketHandler<T> handler
    ) {
        ServerPlayNetworking.registerGlobalReceiver(type, handler);
    }

    @Environment(EnvType.CLIENT)
    public static <T extends FabricPacket> void registerClientReceiver(
        PacketType<T> type,
        ClientPlayNetworking.PlayPacketHandler<T> handler
    ) {
        ClientPlayNetworking.registerGlobalReceiver(type, handler);
    }

    /** STUB(R.Chen): retained for source-compat with legacy call sites that took a Consumer. */
    public static <T extends FabricPacket> void forEachTrackingPlayer(
        ServerWorld world, BlockPos pos, Consumer<ServerPlayerEntity> consumer
    ) {
        PlayerLookup.tracking(world, pos).forEach(consumer);
    }
}
