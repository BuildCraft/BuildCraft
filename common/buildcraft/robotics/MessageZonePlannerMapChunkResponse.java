package buildcraft.robotics;

import io.netty.buffer.ByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.Deque;
import java.util.function.Consumer;

public class MessageZonePlannerMapChunkResponse implements IMessage {
    private ZonePlannerMapChunkKey zonePlannerMapChunkKey = new ZonePlannerMapChunkKey();
    private ZonePlannerMapChunk data = new ZonePlannerMapChunk();

    public MessageZonePlannerMapChunkResponse() {
    }

    public MessageZonePlannerMapChunkResponse(ZonePlannerMapChunkKey zonePlannerMapChunkKey, ZonePlannerMapChunk data) {
        this.zonePlannerMapChunkKey = zonePlannerMapChunkKey;
        this.data = data;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        zonePlannerMapChunkKey.fromBytes(buf);
        data = new ZonePlannerMapChunk();
        int size = buf.readInt();
        for(int i = 0; i < size; i++) {
            BlockPos pos = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
            int colors = buf.readInt();
            data.data.put(pos, colors);
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        zonePlannerMapChunkKey.toBytes(buf);
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
            Deque<Consumer<ZonePlannerMapChunk>> queue = ZonePlannerMapDataClient.instance.pendingRequests.get(message.zonePlannerMapChunkKey);
            if(queue != null) {
                for(Consumer<ZonePlannerMapChunk> consumer : queue) {
                    consumer.accept(message.data);
                }
            }
            return null;
        }
    }
}
