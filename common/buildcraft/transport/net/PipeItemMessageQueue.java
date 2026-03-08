package buildcraft.transport.net;

import buildcraft.lib.net.MessageManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.WeakHashMap;

public class PipeItemMessageQueue {

    private static final Map<ServerPlayer, MessageMultiPipeItem> cachedPlayerPackets = new WeakHashMap<>();

    public static void serverTick() {
        for (Entry<ServerPlayer, MessageMultiPipeItem> entry : cachedPlayerPackets.entrySet()) {
            MessageManager.sendTo(entry.getValue(), entry.getKey());
        }
        cachedPlayerPackets.clear();
    }

    public static void appendTravellingItem(Level world, BlockPos pos, int stackId, byte stackCount, boolean toCenter, Direction side, @Nullable DyeColor colour, byte timeToDest) {
        ServerLevel server = (ServerLevel) world;
//        PlayerChunkMapEntry playerChunkMap = server.getPlayerChunkMap().getEntry(pos.getX() >> 4, pos.getZ() >> 4);
//        if (playerChunkMap == null) {
//            // No-one was watching this chunk.
//            return;
//        }
//        List<EntityPlayerMP> players = new ArrayList<>();
//        // Slightly ugly hack to iterate through all players watching the chunk
//        playerChunkMap.hasPlayerMatchingInRange(0, player -> {
//            players.add(player);
//            // Always return false so that the iteration doesn't stop early
//            return false;
//        });

        List<ServerPlayer> players = server.getChunkSource().chunkMap.getPlayers(new ChunkPos(pos), /*pBoundaryOnly*/ false);
        for (ServerPlayer player : players) {
            cachedPlayerPackets.computeIfAbsent(player, pl -> new MessageMultiPipeItem()).append(pos, stackId,
                    stackCount, toCenter, side, colour, timeToDest);
        }
    }
}
