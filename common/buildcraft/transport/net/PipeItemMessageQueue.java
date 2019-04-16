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

public class PipeItemMessageQueue {

    private static final Map<EntityPlayerMP, MessageMultiPipeItem> cachedPlayerPackets = new WeakHashMap<>();

    public static void serverTick() {
        for (Entry<EntityPlayerMP, MessageMultiPipeItem> entry : cachedPlayerPackets.entrySet()) {
            MessageManager.sendTo(entry.getValue(), entry.getKey());
        }
        cachedPlayerPackets.clear();
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
            cachedPlayerPackets.computeIfAbsent(player, pl -> new MessageMultiPipeItem()).append(pos, stackId,
                stackCount, toCenter, side, colour, timeToDest);
        }
    }
}
