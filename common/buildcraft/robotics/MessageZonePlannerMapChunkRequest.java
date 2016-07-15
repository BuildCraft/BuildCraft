package buildcraft.robotics;

import buildcraft.lib.BCMessageHandler;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageZonePlannerMapChunkRequest implements IMessage {
    private int chunkX;
    private int chunkZ;

    public MessageZonePlannerMapChunkRequest() {
    }

    public MessageZonePlannerMapChunkRequest(int chunkX, int chunkZ) {
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        chunkX = buf.readInt();
        chunkZ = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(chunkX);
        buf.writeInt(chunkZ);
    }

    public static class Handler implements IMessageHandler<MessageZonePlannerMapChunkRequest, IMessage> {
        @Override
        public IMessage onMessage(MessageZonePlannerMapChunkRequest message, MessageContext ctx) {
            ctx.getServerHandler().playerEntity.mcServer.addScheduledTask(() -> {
                ZonePlannerMapDataServer.instance.loadChunk(ctx.getServerHandler().playerEntity.worldObj, message.chunkX, message.chunkZ, zonePlannerMapChunk -> {
                    BCMessageHandler.netWrapper.sendTo(new MessageZonePlannerMapChunkResponse(message.chunkX, message.chunkZ, zonePlannerMapChunk), ctx.getServerHandler().playerEntity);
                });
            });
            return null;
        }
    }
}
