/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc;

import buildcraft.api.core.BCLog;
import buildcraft.api.net.IMessage;
import buildcraft.api.tiles.IBCTileMenuProvider;
import buildcraft.lib.misc.data.DelayedList;
import buildcraft.lib.net.MessageManager;
import buildcraft.lib.net.MessageUpdateTile;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.lib.tile.TileBC_Neptune;
import com.mojang.authlib.GameProfile;
import io.netty.buffer.ByteBuf;
import io.netty.util.internal.StringUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.registries.ForgeRegistries;

import java.lang.reflect.Constructor;
import java.util.*;
import java.util.Map.Entry;

public class MessageUtil {
    private static final DelayedList<Runnable> DELAYED_SERVER_TASKS = DelayedList.createConcurrent();
    private static final DelayedList<Runnable> DELAYED_CLIENT_TASKS = DelayedList.createConcurrent();

    public static void doDelayedServer(Runnable task) {
        doDelayedServer(1, task);
    }

    public static void doDelayedServer(int delay, Runnable task) {
        DELAYED_SERVER_TASKS.add(delay, task);
    }

    public static void doDelayedClient(Runnable task) {
        doDelayedClient(1, task);
    }

    public static void doDelayedClient(int delay, Runnable task) {
        DELAYED_CLIENT_TASKS.add(delay, task);
    }

    public static void postServerTick() {
        for (Runnable runnable : DELAYED_SERVER_TASKS.advance()) {
            runnable.run();
        }
    }

    public static void postClientTick() {
        for (Runnable runnable : DELAYED_CLIENT_TASKS.advance()) {
            runnable.run();
        }
    }

    public static void sendToAllWatching(Level worldObj, BlockPos pos, IMessage message) {
        if (worldObj instanceof ServerLevel server) {
//            PlayerChunkMapEntry playerChunkMap = server.getPlayerChunkMap().getEntry(pos.getX() >> 4, pos.getZ() >> 4);
//            if (playerChunkMap == null) {
//                // No-one was watching this chunk.
//                return;
//            }
//            // Slightly ugly hack to iterate through all players watching the chunk
//            playerChunkMap.hasPlayerMatchingInRange(0, player ->
//            {
//                MessageManager.sendTo(message, player);
//                // Always return false so that the iteration doesn't stop early
//                return false;
//            });
//            // We could just use this instead, but that requires extra packet size as we are wrapping our
//            // packet in an FML packet and sending it through the vanilla system, which is not really desired
//            // playerChunkMap.sendPacket(MessageManager.getPacketFrom(message));

            // Calen: in 1.18.2 use this way
            server.getChunkSource().chunkMap.getPlayers(new ChunkPos(pos),/*pBoundaryOnly*/ false).forEach(p -> MessageManager.sendTo(message, p));
        }
    }

    public static void sendToPlayers(Iterable<Player> players, IMessage message) {
        for (Player player : players) {
//            if (player instanceof EntityPlayerMP)
            if (player instanceof ServerPlayer) {
                MessageManager.sendTo(message, (ServerPlayer) player);
            }
        }
    }

    public static void writeBooleanArray(FriendlyByteBuf buf, boolean[] bool) {
        PacketBufferBC bufBc = PacketBufferBC.asPacketBufferBc(buf);
        for (boolean b : bool) {
            bufBc.writeBoolean(b);
        }
    }

    public static boolean[] readBooleanArray(FriendlyByteBuf buf, int length) {
        boolean[] total = new boolean[length];
        readBooleanArray(buf, total);
        return total;
    }

    public static void readBooleanArray(FriendlyByteBuf buf, boolean[] into) {
        PacketBufferBC bufBc = PacketBufferBC.asPacketBufferBc(buf);
        for (int i = 0; i < into.length; i++) {
            into[i] = bufBc.readBoolean();
        }
    }

    public static void writeBlockPosArray(FriendlyByteBuf buffer, BlockPos[] arr) {
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

    public static BlockPos[] readBlockPosArray(FriendlyByteBuf buffer, int length) {
        BlockPos[] arr = new BlockPos[length];
        boolean[] existsArray = readBooleanArray(buffer, length);
        for (int i = 0; i < length; i++) {
            if (existsArray[i]) {
                arr[i] = MessageUtil.readBlockPos(buffer);
            }
        }
        return arr;
    }

    public static void writeBlockPos(FriendlyByteBuf buffer, BlockPos pos) {
        buffer.writeVarInt(pos.getX());
        buffer.writeVarInt(pos.getY());
        buffer.writeVarInt(pos.getZ());
    }

    public static BlockPos readBlockPos(FriendlyByteBuf buffer) {
        return new BlockPos(buffer.readVarInt(), buffer.readVarInt(), buffer.readVarInt());
    }

    public static void writeVec3d(FriendlyByteBuf buffer, Vec3 vec) {
        buffer.writeDouble(vec.x);
        buffer.writeDouble(vec.y);
        buffer.writeDouble(vec.z);
    }

    public static Vec3 readVec3d(FriendlyByteBuf buffer) {
        return new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
    }

    public static void writeGameProfile(FriendlyByteBuf buffer, GameProfile profile) {
        if (profile != null && profile.isComplete()) {
            buffer.writeBoolean(true);
            buffer.writeUUID(profile.getId());
            buffer.writeUtf(profile.getName());
        } else {
            buffer.writeBoolean(false);
        }
    }

    public static GameProfile readGameProfile(FriendlyByteBuf buffer) {
        if (buffer.readBoolean()) {
            UUID uuid = buffer.readUUID();
            String name = buffer.readUtf(256);
            GameProfile profile = new GameProfile(uuid, name);
            if (profile.isComplete()) {
                return profile;
            }
        }
        return null;
    }

    /** Writes a block state using the block ID and its metadata. Not suitable for full states. */
    public static void writeBlockState(FriendlyByteBuf buf, BlockState state) {
//        buf.writeNbt(NbtUtils.writeBlockState(state));
        Block block = state.getBlock();
        buf.writeResourceLocation(block.getRegistryName());
//        int meta = block.getMetaFromState(state);
//        buf.writeByte(meta);
//        BlockState readState = block.getStateFromMeta(meta);
//        if (readState != state) {
//            buf.writeBoolean(true);
        Map<Property<?>, Comparable<?>> differingProperties = new HashMap<>();
        for (Property<?> property : state.getProperties()) {
            Comparable<?> inputValue = state.getValue(property);
//            Comparable<?> readValue = readState.getValue(property);
//            if (!inputValue.equals(readValue)) {
            differingProperties.put(property, inputValue);
//            }
        }
        buf.writeByte(differingProperties.size());
        for (Entry<Property<?>, Comparable<?>> entry : differingProperties.entrySet()) {
            buf.writeUtf(entry.getKey().getName());
//            buf.writeUtf(entry.getKey().getName(entry.getValue()));
            buf.writeUtf(getName(entry.getKey(), entry.getValue()));
        }
//        } else {
//            buf.writeBoolean(false);
//        }
    }

    /** A copy of {@link NbtUtils#getName(Property, Comparable)} */
    private static <T extends Comparable<T>> String getName(Property<T> p_129211_, Comparable<?> p_129212_) {
        return p_129211_.getName((T) p_129212_);
    }

    public static BlockState readBlockState(FriendlyByteBuf buf) {
//        return NbtUtils.readBlockState(buf.readNbt());
        ResourceLocation id = buf.readResourceLocation();
        Block block = ForgeRegistries.BLOCKS.getValue(id);
//        int meta = buf.readUnsignedByte();
//        IBlockState state = block.getStateFromMeta(meta);
        BlockState state = block.defaultBlockState();
//        if (buf.readBoolean()) {
        int count = buf.readByte();
        for (int p = 0; p < count; p++) {
            String name = buf.readUtf(256);
            String value = buf.readUtf(256);
//            IProperty<?> prop = state.getBlock().getBlockState().getProperty(name);
            Property<?> prop = block.getStateDefinition().getProperty(name);
            state = propertyReadHelper(state, value, prop);
        }
//        }
        return state;
    }

    private static <T extends Comparable<T>> BlockState propertyReadHelper(BlockState state, String value, Property<T> prop) {
        return state.setValue(prop, prop.getValue(value).get());
    }

    /** {@link FriendlyByteBuf#writeEnum(Enum)} can only write *actual* enum values - so not null. This method allows
     * for writing an enum value, or null. */
    public static void writeEnumOrNull(ByteBuf buffer, Enum<?> value) {
        PacketBufferBC buf = PacketBufferBC.asPacketBufferBc(buffer);
        if (value == null) {
            buf.writeBoolean(false);
        } else {
            buf.writeBoolean(true);
            buf.writeEnum(value);
        }
    }

    /** {@link FriendlyByteBuf#readEnum(Class)} can only read *actual* enum values - so not null. This method allows
     * for reading an enum value, or null. */
    public static <E extends Enum<E>> E readEnumOrNull(ByteBuf buffer, Class<E> clazz) {
        PacketBufferBC buf = PacketBufferBC.asPacketBufferBc(buffer);
        if (buf.readBoolean()) {
            return buf.readEnum(clazz);
        } else {
            return null;
        }
    }

    public static <E extends Enum<E>> void writeEnumSet(ByteBuf buffer, Set<E> set, Class<E> clazz) {
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

    public static void sendReturnMessage(NetworkEvent.Context context, IMessage reply) {
        Player player = context.getSender();
//        Player player = BCLibProxy.getProxy().getPlayerForContext(context);
        if (player instanceof ServerPlayer playerMP) {
            MessageManager.sendTo(reply, playerMP);
        } else if (player != null) {
            MessageManager.sendToServer(reply);
        }
    }

    public static FriendlyByteBuf asPacketBuffer(ByteBuf buf) {
        if (buf instanceof FriendlyByteBuf) {
            return (FriendlyByteBuf) buf;
        }
        return new FriendlyByteBuf(buf);
    }

    /** Checks to make sure that this buffer has been *completely* read (so that there are no readable bytes left
     * over */
    public static void ensureEmpty(ByteBuf buf, boolean throwError, String extra) {
        int readableBytes = buf.readableBytes();
        int rb = readableBytes;

        if (buf instanceof PacketBufferBC) {
            // TODO: Find a way of checking if the partial bits have been fully read!
        }

        if (readableBytes > 0) {
            int ri = buf.readerIndex();
            // Get a (small) bit of the data
            byte[] selection = new byte[buf.writerIndex()];
            buf.getBytes(0, selection);
            StringBuilder sb = new StringBuilder("\n");

            for (int i = 0; true; i++) {
                int from = i * 20;
                int to = Math.min(from + 20, selection.length);
                if (from >= to) break;
                byte[] part = Arrays.copyOfRange(selection, from, to);
                for (int j = 0; j < part.length; j++) {
                    byte b = part[j];
                    sb.append(StringUtil.byteToHexStringPadded(b));
                    if (from + j + 1 == ri) {
                        sb.append('#');
                    } else {
                        sb.append(' ');
                    }
                }
                int leftOver = from - to + 20;
                for (int j = 0; j < leftOver; j++) {
                    sb.append("   ");
                }

                sb.append("| ");
                for (byte b : part) {
                    char c = (char) b;
                    if (c < 32 || c > 127) {
                        c = ' ';
                    }
                    sb.append(c);
                }
                sb.append('\n');
            }
            sb.append("-- " + rb);

            IllegalStateException ex = new IllegalStateException("Did not fully read the data! [" + extra + "]" + sb);
            if (throwError) {
                throw ex;
            } else {
                BCLog.logger.warn(ex);
            }
            buf.clear();
        }
    }

    // Calen
    public static boolean clientHandleUpdateTileMsgBeforeOpen(TileBC_Neptune tile, FriendlyByteBuf data, Runnable... additional) {
        MessageUpdateTile msg = new MessageUpdateTile();
        msg.fromBytes(data);
        try {
            // Calen: create a fake Context for tile to read NetworkDirection
            Constructor<NetworkEvent.Context> c = NetworkEvent.Context.class.getDeclaredConstructor(Connection.class, NetworkDirection.class, int.class);
            c.setAccessible(true);
            NetworkEvent.Context ctx = c.newInstance(null, NetworkDirection.PLAY_TO_CLIENT, -1);
            // Process the msg and create a new gate object
            tile.receivePayload(ctx, msg.payload);
            for (Runnable r : additional) {
                r.run();
            }
            return true;
        } catch (Exception e) {
            BCLog.logger.warn("[lib.gui] Failed to handle MessageUpdateTile of Tile[" + tile + "] at " + tile.getBlockPos(), e);
            return false;
        }
    }

    // Calen
    public static void serverOpenTileGui(Player player, IBCTileMenuProvider tile, BlockPos pos) {
        if (player instanceof ServerPlayer serverPlayer) {
            IMessage msg = tile.onServerPlayerOpenNoSend(player);
            NetworkHooks.openGui(
                    serverPlayer, tile, buf ->
                    {
                        buf.writeBlockPos(pos);

                        msg.toBytes(buf);
                    }
            );
        }
    }

    public static <T extends TileBC_Neptune & IBCTileMenuProvider> void serverOpenTileGui(Player player, T tile) {
        if (player instanceof ServerPlayer serverPlayer) {
            IMessage msg = tile.onServerPlayerOpenNoSend(player);
            NetworkHooks.openGui(
                    serverPlayer, tile, buf ->
                    {
                        buf.writeBlockPos(tile.getBlockPos());

                        msg.toBytes(buf);
                    }
            );
        }
    }

    public static void serverOpenGUIWithMsg(Player player, MenuProvider provider, BlockPos pos, int data, IMessage msg) {
        int fullId = data << 8;
        if (player instanceof ServerPlayer serverPlayer) {
            NetworkHooks.openGui(
                    serverPlayer, provider, buf ->
                    {
                        buf.writeBlockPos(pos);
                        buf.writeInt(fullId);

                        msg.toBytes(buf);
                    }
            );
        }
    }

    // Calen
    public static <I extends Item & MenuProvider> void serverOpenItemGui(Player player, I item) {
        if (player instanceof ServerPlayer serverPlayer) {
            NetworkHooks.openGui(serverPlayer, item, serverPlayer.blockPosition());
        }
    }
}
