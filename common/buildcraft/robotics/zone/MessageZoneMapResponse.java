package buildcraft.robotics.zone;

import java.util.Deque;
import java.util.function.Consumer;

import net.minecraft.network.PacketBuffer;

import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import io.netty.buffer.ByteBuf;

public class MessageZoneMapResponse implements IMessage {
    private ZonePlannerMapChunkKey key;
    private ZonePlannerMapChunk data;

    public MessageZoneMapResponse() {}

    public MessageZoneMapResponse(ZonePlannerMapChunkKey zonePlannerMapChunkKey, ZonePlannerMapChunk data) {
        this.key = zonePlannerMapChunkKey;
        this.data = data;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        key = new ZonePlannerMapChunkKey(buf);
        data = new ZonePlannerMapChunk(new PacketBuffer(buf));
    }

    @Override
    public void toBytes(ByteBuf buf) {
        key.toBytes(buf);
        data.write(new PacketBuffer(buf));
    }

    public static enum Handler implements IMessageHandler<MessageZoneMapResponse, IMessage> {
        INSTANCE;

        @Override
        public IMessage onMessage(MessageZoneMapResponse message, MessageContext ctx) {
            Deque<Consumer<ZonePlannerMapChunk>> queue = ZonePlannerMapDataClient.INSTANCE.pendingRequests.get(message.key);
            if (queue != null) {
                while (!queue.isEmpty()) {
                    queue.remove().accept(message.data);
                }
            }
            return null;
        }
    }
}
