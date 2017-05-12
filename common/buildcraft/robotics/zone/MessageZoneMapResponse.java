package buildcraft.robotics.zone;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;

public class MessageZoneMapResponse implements IMessage {
    private ZonePlannerMapChunkKey key;
    private ZonePlannerMapChunk data;

    public MessageZoneMapResponse() {
    }

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

    public static final IMessageHandler<MessageZoneMapResponse, IMessage> HANDLER = (message, ctx) -> {
        ZonePlannerMapDataClient.INSTANCE.onChunkReceived(message.key, message.data);
        return null;
    };
}
