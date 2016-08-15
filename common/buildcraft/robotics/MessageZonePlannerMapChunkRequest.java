package buildcraft.robotics;

import buildcraft.lib.BCMessageHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.util.math.ChunkPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageZonePlannerMapChunkRequest implements IMessage {
    private ChunkPos chunkPos;

    public MessageZonePlannerMapChunkRequest() {
    }

    public MessageZonePlannerMapChunkRequest(ChunkPos chunkPos) {
        this.chunkPos = chunkPos;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        chunkPos = new ChunkPos(buf.readInt(), buf.readInt());
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(chunkPos.chunkXPos);
        buf.writeInt(chunkPos.chunkZPos);
    }

    public static enum Handler implements IMessageHandler<MessageZonePlannerMapChunkRequest, IMessage> {
        INSTANCE;

        @Override
        public IMessage onMessage(MessageZonePlannerMapChunkRequest message, MessageContext ctx) {
            ZonePlannerMapDataServer.instance.getChunk(ctx.getServerHandler().playerEntity.worldObj, message.chunkPos, ctx.getServerHandler().playerEntity.worldObj.provider.getDimension(), zonePlannerMapChunk -> {
                BCMessageHandler.netWrapper.sendTo(new MessageZonePlannerMapChunkResponse(message.chunkPos, zonePlannerMapChunk), ctx.getServerHandler().playerEntity);
            });
            return null;
        }
    }
}
