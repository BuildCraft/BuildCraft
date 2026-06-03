/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc;

import java.util.UUID;

import com.mojang.authlib.GameProfile;

import io.netty.buffer.ByteBuf;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import buildcraft.api.core.BCLog;

import buildcraft.lib.net.BCNetworkManager;
import buildcraft.lib.net.MessageManager;
import buildcraft.lib.net.MessageUpdateTile;

// STUB(R.Chen): the bulk of the Forge MessageUtil (PacketByteBuf array helpers, block-state (de)serialise,
// the DelayedList client-send queue, and the Forge PlayerChunkMap watcher iteration) is deferred. Only the
// send helpers + GameProfile (de)serialise + ensureEmpty used by TileBC_Neptune are migrated. The watcher
// broadcast now routes through BCNetworkManager's Fabric PlayerLookup.tracking helper.
public class MessageUtil {

    public static void sendToAllWatching(World world, BlockPos pos, MessageUpdateTile message) {
        BCNetworkManager.sendToAllInWorld(message, world, pos);
    }

    public static void sendToPlayers(Iterable<PlayerEntity> players, MessageUpdateTile message) {
        for (PlayerEntity player : players) {
            if (player instanceof ServerPlayerEntity) {
                MessageManager.sendTo(message, (ServerPlayerEntity) player);
            }
        }
    }

    public static void writeGameProfile(PacketByteBuf buffer, GameProfile profile) {
        if (profile != null && profile.isComplete()) {
            buffer.writeBoolean(true);
            buffer.writeUuid(profile.getId());
            buffer.writeString(profile.getName());
        } else {
            buffer.writeBoolean(false);
        }
    }

    public static GameProfile readGameProfile(PacketByteBuf buffer) {
        if (buffer.readBoolean()) {
            UUID uuid = buffer.readUuid();
            String name = buffer.readString(256);
            GameProfile profile = new GameProfile(uuid, name);
            if (profile.isComplete()) {
                return profile;
            }
        }
        return null;
    }

    public static <E extends Enum<E>> void writeEnumOrNull(PacketByteBuf buffer, E value) {
        if (value == null) {
            buffer.writeByte(0);
        } else {
            buffer.writeByte(value.ordinal() + 1);
        }
    }

    public static <E extends Enum<E>> E readEnumOrNull(PacketByteBuf buffer, Class<E> enumClass) {
        int ordinal = buffer.readUnsignedByte();
        if (ordinal == 0) return null;
        E[] constants = enumClass.getEnumConstants();
        int idx = ordinal - 1;
        return (idx >= 0 && idx < constants.length) ? constants[idx] : null;
    }

    // ---- BlockPos helpers (compat with 1.12.2 API) ----
    public static void writeBlockPos(PacketByteBuf buf, BlockPos pos) {
        buf.writeBlockPos(pos);
    }

    public static BlockPos readBlockPos(PacketByteBuf buf) {
        return buf.readBlockPos();
    }

    /** Checks that the given buffer has been fully read; logs (or throws) if bytes remain. */
    public static void ensureEmpty(ByteBuf buf, boolean throwError, String extra) {
        int readableBytes = buf.readableBytes();
        if (readableBytes > 0) {
            String message = "Did not fully read the buffer! (" + readableBytes + " bytes remain) " + extra;
            if (throwError) {
                throw new IllegalStateException(message);
            } else {
                BCLog.logger.warn("[lib.net] " + message);
            }
        }
    }
}
