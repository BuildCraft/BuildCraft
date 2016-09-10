package buildcraft.robotics.zone;

import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import buildcraft.lib.misc.MessageUtil;

import io.netty.buffer.ByteBuf;

public class MessageZoneMapRequest implements IMessage {
    private ZonePlannerMapChunkKey key;

    public MessageZoneMapRequest() {}

    public MessageZoneMapRequest(ZonePlannerMapChunkKey key) {
        this.key = key;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        key = new ZonePlannerMapChunkKey(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        key.toBytes(buf);
    }

    public static enum Handler implements IMessageHandler<MessageZoneMapRequest, IMessage> {
        INSTANCE;

        @Override
        public IMessage onMessage(MessageZoneMapRequest message, MessageContext ctx) {
            ZonePlannerMapDataServer.INSTANCE.getChunk(ctx.getServerHandler().playerEntity.worldObj, message.key, data -> {
                MessageUtil.sendReturnMessage(ctx, new MessageZoneMapResponse(message.key, data));
            });
            return null;
        }
    }
}
