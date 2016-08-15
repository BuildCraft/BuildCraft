package buildcraft.robotics;

import io.netty.buffer.ByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Deque;
import java.util.Map;
import java.util.function.Consumer;

public class MessageZonePlannerMapChunkResponse implements IMessage {
    private ChunkPos chunkPos;
    private ZonePlannerMapChunk data;

    public MessageZonePlannerMapChunkResponse() {
    }

    public MessageZonePlannerMapChunkResponse(ChunkPos chunkPos, ZonePlannerMapChunk data) {
        this.chunkPos = chunkPos;
        this.data = data;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        chunkPos = new ChunkPos(buf.readInt(), buf.readInt());
        data = new ZonePlannerMapChunk();
        data.dimensionalId = buf.readInt();
        int size = buf.readInt();
        for(int i = 0; i < size; i++) {
            BlockPos pos = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
            int colors = buf.readInt();
            data.data.put(pos, colors);
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(chunkPos.chunkXPos);
        buf.writeInt(chunkPos.chunkZPos);
        buf.writeInt(data.dimensionalId);
        buf.writeInt(data.data.size());
        for(BlockPos pos : data.data.keySet()) {
            int color = data.data.get(pos);
            buf.writeInt(pos.getX());
            buf.writeInt(pos.getY());
            buf.writeInt(pos.getZ());
            buf.writeInt(color);
        }
    }

    public static enum Handler implements IMessageHandler<MessageZonePlannerMapChunkResponse, IMessage> {
        INSTANCE;

        @Override
        public IMessage onMessage(MessageZonePlannerMapChunkResponse message, MessageContext ctx) {
            Deque<Consumer<ZonePlannerMapChunk>> queue = ZonePlannerMapDataClient.instance.pendingRequests.get(Pair.of(message.chunkPos, message.data.dimensionalId));
            if(queue != null) {
                for(Consumer<ZonePlannerMapChunk> consumer : queue) {
                    consumer.accept(message.data);
                }
            }
            return null;
        }
    }
}
