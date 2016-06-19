package buildcraft.lib.misc;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraft.server.management.PlayerChunkMapEntry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;

import buildcraft.lib.BCMessageHandler;
import buildcraft.lib.misc.data.DelayedList;

public class MessageUtil {
    private static final DelayedList<Runnable> DELAYED = DelayedList.createConcurrent();

    public static SimpleNetworkWrapper getWrapper() {
        return BCMessageHandler.netWrapper;
    }

    public static void doDelayed(Runnable task) {
        doDelayed(1, task);
    }

    public static void doDelayed(int delay, Runnable task) {
        DELAYED.add(delay, task);
    }

    public static void preTick() {
        for (Runnable runnable : DELAYED.advance()) {
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
        int byteLength = MathHelper.ceiling_double_int(bool.length / 8.0);
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
        int bytes = MathHelper.ceiling_double_int(length / 8.0);
        for (int b = 0; b < bytes; b++) {
            short packed = buf.readUnsignedByte();
            for (int i = 0; i < 8 && i + b * 8 < total.length; i++) {
                int mask = 1 << i;
                if ((packed & mask) == mask) {
                    total[i + b * 8] = true;
                }
            }
        }
        return total;
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
}
