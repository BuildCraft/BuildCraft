package buildcraft.lib.net;

import buildcraft.api.core.BCLog;
import buildcraft.api.net.IMessage;
import buildcraft.api.net.IMessageHandler;
import buildcraft.lib.BCLibProxy;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import java.io.IOException;
import java.util.UUID;

public class MessageUpdateEntity implements IMessage {
    private UUID uuid;
    // private PacketBufferBC payload;
    public PacketBufferBC payload;

    @SuppressWarnings("unused")
    public MessageUpdateEntity() {
    }

    public MessageUpdateEntity(Entity entity, PacketBufferBC payload) {
        this.uuid = entity.getUUID();
        this.payload = payload;
        if (getPayloadSize() > 1 << 24) {
            throw new IllegalStateException("Can't write out " + getPayloadSize() + "bytes!");
        }
    }

    public int getPayloadSize() {
        return payload == null ? 0 : payload.readableBytes();
    }

    @Override
    public void fromBytes(FriendlyByteBuf buf) {
        this.uuid = buf.readUUID();
        int size = buf.readUnsignedMedium();
        payload = new PacketBufferBC(buf.readBytes(size));
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(this.uuid);
        int length = payload.readableBytes();
        buf.writeMedium(length);
        buf.writeBytes(payload, 0, length);
    }

    public static final IMessageHandler<MessageUpdateEntity, IMessage> HANDLER = (message, ctx) ->
    {
        try {
            Player player = BCLibProxy.getProxy().getPlayerForContext(ctx);
            if (player == null || player.level == null) {
                return null;
            }
            Entity entity = player.level.getEntities().get(message.uuid);
            if (entity instanceof IPayloadReceiver) {
                return ((IPayloadReceiver) entity).receivePayload(ctx, message.payload);
            } else {
                BCLog.logger.warn("Dropped message for entity " + message.uuid);
            }
            return null;
        } catch (IOException io) {
            throw new RuntimeException(io);
        } finally {
            message.payload.release();
        }
    };
}
