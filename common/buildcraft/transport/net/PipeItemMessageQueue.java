package buildcraft.transport.net;

import buildcraft.lib.net.MessageManager;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.DyeColor;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.WeakHashMap;
import java.util.stream.Collectors;

public class PipeItemMessageQueue {

    private static final Map<ServerPlayerEntity, MessageMultiPipeItem> cachedPlayerPackets = new WeakHashMap<>();

    public static void serverTick() {
        for (Entry<ServerPlayerEntity, MessageMultiPipeItem> entry : cachedPlayerPackets.entrySet()) {
            MessageManager.sendTo(entry.getValue(), entry.getKey());
        }
        cachedPlayerPackets.clear();
    }

    public static void appendTravellingItem(World world, BlockPos pos, int stackId, byte stackCount, boolean toCenter, Direction side, @Nullable DyeColor colour, byte timeToDest) {
        ServerWorld server = (ServerWorld) world;
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

        List<ServerPlayerEntity> players = server.getChunkSource().chunkMap.getPlayers(new ChunkPos(pos), /*pBoundaryOnly*/ false).collect(Collectors.toList());
        for (ServerPlayerEntity player : players) {
            cachedPlayerPackets.computeIfAbsent(player, pl -> new MessageMultiPipeItem()).append(pos, stackId,
                    stackCount, toCenter, side, colour, timeToDest);
        }
    }
}
