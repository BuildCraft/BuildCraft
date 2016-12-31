package buildcraft.lib.misc;

import java.util.UUID;

import com.mojang.authlib.GameProfile;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraft.server.management.PlayerChunkMapEntry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;

import buildcraft.api.core.BCLog;

import buildcraft.lib.BCLibProxy;
import buildcraft.lib.BCMessageHandler;
import buildcraft.lib.misc.data.DelayedList;
import buildcraft.lib.net.PacketBufferBC;

import io.netty.buffer.ByteBuf;

public class MessageUtil {
    private static final DelayedList<Runnable> DELAYED_TASKS = DelayedList.createConcurrent();

    public static SimpleNetworkWrapper getWrapper() {
        return BCMessageHandler.netWrapper;
    }

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
                getWrapper().sendTo(message, player);
                // Always return false so that the iteration doesn't stop early
                return false;
            });
            // We could just use this instead, but that requires extra packet size as we are wrapping our
            // packet in an FML packet and sending it through the vanilla system, which is not really desired
            /** playerChunkMap.sendPacket(getWrapper().getPacketFrom(message)); */
        }
    }

    public static void sendToPlayers(Iterable<EntityPlayer> players, IMessage message) {
        for (EntityPlayer player : players) {
            if (player instanceof EntityPlayerMP) {
                getWrapper().sendTo(message, (EntityPlayerMP) player);
            }
        }
    }

    public static void writeBooleanArray(PacketBuffer buf, boolean[] bool) {
        int byteLength = MathHelper.ceil(bool.length / 8.0);
        for (int b = 0; b < byteLength; b++) {
            short total = 0;
            for (int i = 0; i < 8 && i + b * 8 < bool.length; i++) {
                if (bool[i + b * 8]) {
                    total |= 1 << i;
                }
            }
            buf.writeByte(total);
        }
    }

    public static boolean[] readBooleanArray(PacketBuffer buf, int length) {
        boolean[] total = new boolean[length];
        readBooleanArray(buf, total);
        return total;
    }

    public static void readBooleanArray(PacketBuffer buf, boolean[] into) {
        int bytes = MathHelper.ceil(into.length / 8.0);
        for (int b = 0; b < bytes; b++) {
            short packed = buf.readUnsignedByte();
            for (int i = 0; i < 8 && i + b * 8 < into.length; i++) {
                int mask = 1 << i;
                into[i + b * 8] = (packed & mask) == mask;
            }
        }
    }

    public static void writeNullableBlockPos(PacketBuffer buffer, BlockPos pos) {
        if (pos != null) {
            buffer.writeBlockPos(pos);
        }
    }

    public static BlockPos readNullableBlockPos(PacketBuffer buffer, boolean exists) {
        if (exists) {
            return buffer.readBlockPos();
        } else {
            return null;
        }
    }

    public static void writeBlockPosArray(PacketBuffer buffer, BlockPos[] arr) {
        boolean[] existsArray = new boolean[arr.length];
        for (int i = 0; i < arr.length; i++) {
            existsArray[i] = arr[i] != null;
        }
        writeBooleanArray(buffer, existsArray);
        for (BlockPos pos : arr) {
            writeNullableBlockPos(buffer, pos);
        }
    }

    public static BlockPos[] readBlockPosArray(PacketBuffer buffer, int length) {
        BlockPos[] arr = new BlockPos[length];
        boolean[] existsArray = readBooleanArray(buffer, length);
        for (int i = 0; i < length; i++) {
            arr[i] = readNullableBlockPos(buffer, existsArray[i]);
        }
        return arr;
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

    public static void sendReturnMessage(MessageContext context, IMessage reply) {
        EntityPlayer player = BCLibProxy.getProxy().getPlayerForContext(context);
        if (player instanceof EntityPlayerMP) {
            EntityPlayerMP playerMP = (EntityPlayerMP) player;
            BCMessageHandler.netWrapper.sendTo(reply, playerMP);
        } else if (player != null) {
            BCMessageHandler.netWrapper.sendToServer(reply);
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
                sb.append(" (+" + (readableBytes - 10) + ")");
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
