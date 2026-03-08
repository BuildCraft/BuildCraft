package buildcraft.lib.net;

import buildcraft.api.core.BCLog;
import buildcraft.api.net.IMessage;
import buildcraft.api.net.IMessageHandler;
import buildcraft.lib.BCLibProxy;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.server.ServerWorld;

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
    public void fromBytes(PacketBuffer buf) {
        this.uuid = buf.readUUID();
        int size = buf.readUnsignedMedium();
        payload = new PacketBufferBC(buf.readBytes(size));
    }

    @Override
    public void toBytes(PacketBuffer buf) {
        buf.writeUUID(this.uuid);
        int length = payload.readableBytes();
        buf.writeMedium(length);
        buf.writeBytes(payload, 0, length);
    }

    public static final IMessageHandler<MessageUpdateEntity, IMessage> HANDLER = (message, ctx) ->
    {
        try {
            PlayerEntity player = BCLibProxy.getProxy().getPlayerForContext(ctx);
            if (player == null || player.level == null) {
                return null;
            }
            Entity entity;
            if (player.level instanceof ServerWorld) {
                entity = ((ServerWorld) player.level).getEntity(message.uuid);
            } else if (player.level instanceof ClientWorld) {
                entity = ((ClientWorld) player.level).entitiesById.values().stream()
                        .filter(e -> e.getUUID().equals(message.uuid))
                        .findFirst()
                        .orElse(null);
            } else {
                entity = null;
            }
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
