package buildcraft.robotics;

import buildcraft.lib.BCMessageHandler;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageZonePlannerMapChunkRequest implements IMessage {
    private ZonePlannerMapChunkKey zonePlannerMapChunkKey = new ZonePlannerMapChunkKey();

    public MessageZonePlannerMapChunkRequest() {
    }

    public MessageZonePlannerMapChunkRequest(ZonePlannerMapChunkKey zonePlannerMapChunkKey) {
        this.zonePlannerMapChunkKey = zonePlannerMapChunkKey;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        zonePlannerMapChunkKey.fromBytes(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        zonePlannerMapChunkKey.toBytes(buf);
    }

    public static enum Handler implements IMessageHandler<MessageZonePlannerMapChunkRequest, IMessage> {
        INSTANCE;

        @Override
        public IMessage onMessage(MessageZonePlannerMapChunkRequest message, MessageContext ctx) {
            ZonePlannerMapDataServer.instance.getChunk(ctx.getServerHandler().playerEntity.worldObj, message.zonePlannerMapChunkKey, zonePlannerMapChunk -> {
                BCMessageHandler.netWrapper.sendTo(new MessageZonePlannerMapChunkResponse(message.zonePlannerMapChunkKey, zonePlannerMapChunk), ctx.getServerHandler().playerEntity);
            });
            return null;
        }
    }
}
