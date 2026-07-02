package ct.buildcraft.transport.net;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.WeakHashMap;

import javax.annotation.Nullable;

import ct.buildcraft.lib.net.MessageManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;

public class PipeItemMessageQueue {

    private static final Map<LevelChunk, List<MessageMultiPipeItem>> cachedPlayerPackets = new WeakHashMap<>();

    
    public static void serverTick() {
        for (Entry<LevelChunk, List<MessageMultiPipeItem>> entry : cachedPlayerPackets.entrySet()) {
        	LevelChunk chunk = entry.getKey();
        	for(MessageMultiPipeItem msg : entry.getValue()) {
        		MessageManager.sendToAllWatching(msg, chunk);
        	}
        }
        cachedPlayerPackets.clear();
    }

    public static void appendTravellingItem(Level world, BlockPos pos, int stackId, byte stackCount, boolean toCenter,
        Direction side, @Nullable DyeColor colour, byte timeToDest) {
        ServerLevel server = (ServerLevel) world;
		MessageMultiPipeItem msg = new MessageMultiPipeItem();
		msg.append(pos, stackId, stackCount, toCenter, side, colour, timeToDest);
        cachedPlayerPackets.computeIfAbsent(server.getChunkAt(pos), 
        	pl -> new ArrayList<>()
        ).add(msg);

    }
}
