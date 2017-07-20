/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc;

import buildcraft.api.core.BCLog;
import buildcraft.lib.BCLibProxy;
import buildcraft.lib.misc.data.DelayedList;
import buildcraft.lib.net.MessageManager;
import buildcraft.lib.net.PacketBufferBC;
import com.mojang.authlib.GameProfile;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraft.server.management.PlayerChunkMapEntry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.EnumSet;
import java.util.UUID;

public class MessageUtil {
    private static final DelayedList<Runnable> DELAYED_TASKS = DelayedList.createConcurrent();

    public static void doDelayed(Runnable task) {
        doDelayed(1, task);
    }

    public static void doDelayed(int delay, Runnable task) {
        DELAYED_TASKS.add(delay, task);
    }

    public static void postTick() {
        for (Runnable runnable : DELAYED_TASKS.advance()) {
            runnable.run();
        }
    }

    public static void sendToAllWatching(World worldObj, BlockPos pos, IMessage message) {
        if (worldObj instanceof WorldServer) {
            WorldServer server = (WorldServer) worldObj;
            PlayerChunkMapEntry playerChunkMap = server.getPlayerChunkMap().getEntry(pos.getX() >> 4, pos.getZ() >> 4);
            if (playerChunkMap == null) {
                // No-one was watching this chunk.
                return;
            }
            // Slightly ugly hack to iterate through all players watching the chunk
            playerChunkMap.hasPlayerMatchingInRange(0, player -> {
                MessageManager.sendTo(message, player);
                // Always return false so that the iteration doesn't stop early
                return false;
            });
            // We could just use this instead, but that requires extra packet size as we are wrapping our
            // packet in an FML packet and sending it through the vanilla system, which is not really desired
            // playerChunkMap.sendPacket(MessageManager.getPacketFrom(message));
        }
    }

    public static void sendToPlayers(Iterable<EntityPlayer> players, IMessage message) {
        for (EntityPlayer player : players) {
            if (player instanceof EntityPlayerMP) {
                MessageManager.sendTo(message, (EntityPlayerMP) player);
            }
        }
    }

    public static void writeBooleanArray(PacketBuffer buf, boolean[] bool) {
        PacketBufferBC bufBc = PacketBufferBC.asPacketBufferBc(buf);
        for (boolean b : bool) {
            bufBc.writeBoolean(b);
        }
    }

    public static boolean[] readBooleanArray(PacketBuffer buf, int length) {
        boolean[] total = new boolean[length];
        readBooleanArray(buf, total);
        return total;
    }

    public static void readBooleanArray(PacketBuffer buf, boolean[] into) {
        PacketBufferBC bufBc = PacketBufferBC.asPacketBufferBc(buf);
        for (int i = 0; i < into.length; i++) {
            into[i] = bufBc.readBoolean();
        }
    }

    public static void writeBlockPosArray(PacketBuffer buffer, BlockPos[] arr) {
        boolean[] existsArray = new boolean[arr.length];
        for (int i = 0; i < arr.length; i++) {
            existsArray[i] = arr[i] != null;
        }
        writeBooleanArray(buffer, existsArray);
        for (BlockPos pos : arr) {
            if (pos != null) {
                MessageUtil.writeBlockPos(buffer, pos);
            }
        }
    }

    public static BlockPos[] readBlockPosArray(PacketBuffer buffer, int length) {
        BlockPos[] arr = new BlockPos[length];
        boolean[] existsArray = readBooleanArray(buffer, length);
        for (int i = 0; i < length; i++) {
            if (existsArray[i]) {
                arr[i] = MessageUtil.readBlockPos(buffer);
            }
        }
        return arr;
    }

    public static void writeBlockPos(PacketBuffer buffer, BlockPos pos) {
        buffer.writeVarInt(pos.getX());
        buffer.writeVarInt(pos.getY());
        buffer.writeVarInt(pos.getZ());
    }

    public static BlockPos readBlockPos(PacketBuffer buffer) {
        return new BlockPos(buffer.readVarInt(), buffer.readVarInt(), buffer.readVarInt());
    }

    public static void writeVec3d(PacketBuffer buffer, Vec3d vec) {
        buffer.writeDouble(vec.xCoord);
        buffer.writeDouble(vec.yCoord);
        buffer.writeDouble(vec.zCoord);
    }

    public static Vec3d readVec3d(PacketBuffer buffer) {
        return new Vec3d(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
    }

    public static void writeGameProfile(PacketBuffer buffer, GameProfile profile) {
        if (profile != null && profile.isComplete()) {
            buffer.writeBoolean(true);
            buffer.writeUniqueId(profile.getId());
            buffer.writeString(profile.getName());
        } else {
            buffer.writeBoolean(false);
        }
    }

    public static GameProfile readGameProfile(PacketBuffer buffer) {
        if (buffer.readBoolean()) {
            UUID uuid = buffer.readUniqueId();
            String name = buffer.readString(256);
            GameProfile profile = new GameProfile(uuid, name);
            if (profile.isComplete()) {
                return profile;
            }
        }
        return null;
    }

    /** Writes a block state using the block ID and its metadata. Not suitable for full states. */
    public static void writeBlockState(PacketBuffer buf, IBlockState state) {
        buf.writeVarInt(Block.REGISTRY.getIDForObject(state.getBlock()));
        buf.writeVarInt(state.getBlock().getMetaFromState(state));
    }

    public static IBlockState readBlockState(PacketBuffer buf) {
        int id = buf.readVarInt();
        int meta = buf.readVarInt();
        Block block = Block.REGISTRY.getObjectById(id);
        return block.getStateFromMeta(meta);
    }

    /** {@link PacketBuffer#writeEnumValue(Enum)} can only write *actual* enum values - so not null. This method allows
     * for writing an enum value, or null. */
    public static void writeEnumOrNull(ByteBuf buffer, Enum<?> value) {
        PacketBufferBC buf = PacketBufferBC.asPacketBufferBc(buffer);
        if (value == null) {
            buf.writeBoolean(false);
        } else {
            buf.writeBoolean(true);
            buf.writeEnumValue(value);
        }
    }

    /** {@link PacketBuffer#readEnumValue(Class)} can only read *actual* enum values - so not null. This method allows
     * for reading an enum value, or null. */
    public static <E extends Enum<E>> E readEnumOrNull(ByteBuf buffer, Class<E> clazz) {
        PacketBufferBC buf = PacketBufferBC.asPacketBufferBc(buffer);
        if (buf.readBoolean()) {
            return buf.readEnumValue(clazz);
        } else {
            return null;
        }
    }

    public static <E extends Enum<E>> void writeEnumSet(ByteBuf buffer, EnumSet<E> set, Class<E> clazz) {
        PacketBufferBC buf = PacketBufferBC.asPacketBufferBc(buffer);
        E[] constants = clazz.getEnumConstants();
        if (constants == null) throw new IllegalArgumentException("Not an enum type " + clazz);
        for (E e : constants) {
            buf.writeBoolean(set.contains(e));
        }
    }

    public static <E extends Enum<E>> EnumSet<E> readEnumSet(ByteBuf buffer, Class<E> clazz) {
        PacketBufferBC buf = PacketBufferBC.asPacketBufferBc(buffer);
        E[] constants = clazz.getEnumConstants();
        if (constants == null) throw new IllegalArgumentException("Not an enum type " + clazz);
        EnumSet<E> set = EnumSet.noneOf(clazz);
        for (E e : constants) {
            if (buf.readBoolean()) {
                set.add(e);
            }
        }
        return set;
    }

    public static void sendReturnMessage(MessageContext context, IMessage reply) {
        EntityPlayer player = BCLibProxy.getProxy().getPlayerForContext(context);
        if (player instanceof EntityPlayerMP) {
            EntityPlayerMP playerMP = (EntityPlayerMP) player;
            MessageManager.sendTo(reply, playerMP);
        } else if (player != null) {
            MessageManager.sendToServer(reply);
        }
    }

    public static PacketBuffer asPacketBuffer(ByteBuf buf) {
        if (buf instanceof PacketBuffer) {
            return (PacketBuffer) buf;
        }
        return new PacketBuffer(buf);
    }

    /** Checks to make sure that this buffer has been *completely* read (so that there are no readable bytes left
     * over */
    public static void ensureEmpty(ByteBuf buf, boolean throwError, String extra) {
        int readableBytes = buf.readableBytes();
        if (readableBytes > 0) {
            // Get a (small) bit of the data
            byte[] selection = new byte[readableBytes > 10 ? 10 : readableBytes];
            buf.readBytes(selection);
            StringBuilder sb = new StringBuilder();
            for (byte b : selection) {
                String h = Integer.toHexString(Byte.toUnsignedInt(b));
                if (h.length() == 1) {
                    sb.append(" 0");
                } else {
                    sb.append(" ");
                }
                sb.append(h);
            }
            if (readableBytes > 10) {
                sb.append(" (+").append(readableBytes - 10).append(")");
            }

            IllegalStateException ex = new IllegalStateException("Did not fully read the data! [" + extra + "]" + sb);
            if (throwError) {
                throw ex;
            } else {
                BCLog.logger.warn(ex);
            }
            buf.clear();
        }
    }
}
