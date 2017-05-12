package buildcraft.robotics.zone;

import buildcraft.lib.misc.MessageUtil;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;

public class MessageZoneMapRequest implements IMessage {
    private ZonePlannerMapChunkKey key;

    public MessageZoneMapRequest() {
    }

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

    public static final IMessageHandler<MessageZoneMapRequest, IMessage> HANDLER = (message, ctx) -> {
        MessageUtil.sendReturnMessage(
                ctx,
                new MessageZoneMapResponse(
                        message.key,
                        ZonePlannerMapDataServer.INSTANCE.getChunk(
                                ctx.getServerHandler().player.world,
                                message.key
                        )
                )
        );
        return null;
    };
}
