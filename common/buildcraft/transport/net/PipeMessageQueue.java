package buildcraft.transport.net;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.WeakHashMap;

import javax.annotation.Nullable;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.server.management.PlayerChunkMapEntry;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import buildcraft.lib.net.MessageManager;

public class PipeMessageQueue {

    private static final Map<EntityPlayerMP, MessageMultiPipeItem> cachedPlayerItemPackets = new WeakHashMap<>();
    private static final Map<EntityPlayerMP, MessageMultiPipeFluid> cachedPlayerFluidPackets = new WeakHashMap<>();

    public static void serverTick() {
        for (Entry<EntityPlayerMP, MessageMultiPipeItem> entry : cachedPlayerItemPackets.entrySet()) {
            MessageManager.sendTo(entry.getValue(), entry.getKey());
        }
        for (Entry<EntityPlayerMP, MessageMultiPipeFluid> entry : cachedPlayerFluidPackets.entrySet()) {
            MessageManager.sendTo(entry.getValue(), entry.getKey());
        }
        cachedPlayerItemPackets.clear();
        cachedPlayerFluidPackets.clear();
    }

    public static void appendTravellingItem(World world, BlockPos pos, int stackId, byte stackCount, boolean toCenter,
        EnumFacing side, @Nullable EnumDyeColor colour, byte timeToDest) {
        WorldServer server = (WorldServer) world;
        PlayerChunkMapEntry playerChunkMap = server.getPlayerChunkMap().getEntry(pos.getX() >> 4, pos.getZ() >> 4);
        if (playerChunkMap == null) {
            // No-one was watching this chunk.
            return;
        }
        List<EntityPlayerMP> players = new ArrayList<>();
        // Slightly ugly hack to iterate through all players watching the chunk
        playerChunkMap.hasPlayerMatchingInRange(0, player -> {
            players.add(player);
            // Always return false so that the iteration doesn't stop early
            return false;
        });
        for (EntityPlayerMP player : players) {
            cachedPlayerItemPackets.computeIfAbsent(player, pl -> new MessageMultiPipeItem()).append(
                pos, stackId, stackCount, toCenter, side, colour, timeToDest
            );
        }
    }

    public static boolean appendFluids(World world, BlockPos pos, short[] amounts, byte[] dirs) {
        WorldServer server = (WorldServer) world;
        PlayerChunkMapEntry playerChunkMap = server.getPlayerChunkMap().getEntry(pos.getX() >> 4, pos.getZ() >> 4);
        if (playerChunkMap == null) {
            // No-one was watching this chunk.
            return true;
        }
        List<EntityPlayerMP> players = new ArrayList<>();
        // Slightly ugly hack to iterate through all players watching the chunk
        playerChunkMap.hasPlayerMatchingInRange(0, player -> {
            players.add(player);
            // Always return false so that the iteration doesn't stop early
            return false;
        });
        boolean all = true;
        for (EntityPlayerMP player : players) {
            MessageMultiPipeFluid message = cachedPlayerFluidPackets.computeIfAbsent(
                player, pl -> new MessageMultiPipeFluid()
            );
            all &= message.append(pos, amounts, dirs);
        }
        return all;
    }
}
