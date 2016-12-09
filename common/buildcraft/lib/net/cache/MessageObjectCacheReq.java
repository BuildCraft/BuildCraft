package buildcraft.lib.net.cache;

import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import buildcraft.lib.net.PacketBufferBC;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/** Signifies a client->server request for the value of a cached object, given its ID. */
public class MessageObjectCacheReq implements IMessage {

    private int cacheId;

    private int[] ids;

    /** Used by forge automatically to construct the message. Do not use! */
    @Deprecated
    public MessageObjectCacheReq() {}

    MessageObjectCacheReq(NetworkedObjectCache<?> cache, int[] ids) {
        this.cacheId = BuildCraftObjectCaches.CACHES.indexOf(cache);
        this.ids = ids;
        if (ids.length > Short.MAX_VALUE) {
            throw new IllegalStateException("Tried to request too many ID's! (" + ids.length + ")");
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeByte(cacheId);
        buf.writeShort(ids.length);
        for (int id : ids) {
            buf.writeInt(id);
        }
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        cacheId = buf.readByte();
        int idCount = buf.readShort();
        ids = new int[idCount];
        for (int i = 0; i < idCount; i++) {
            ids[i] = buf.readInt();
        }
    }

    public enum Handler implements IMessageHandler<MessageObjectCacheReq, MessageObjectCacheReply> {
        INSTANCE;

        @Override
        public MessageObjectCacheReply onMessage(MessageObjectCacheReq message, MessageContext ctx) {
            NetworkedObjectCache<?> cache = BuildCraftObjectCaches.CACHES.get(message.cacheId);
            byte[][] values = new byte[message.ids.length][];

            PacketBufferBC buffer = new PacketBufferBC(Unpooled.buffer());
            for (int i = 0; i < values.length; i++) {
                int id = message.ids[i];
                cache.writeObjectServer(id, buffer);
                values[i] = new byte[buffer.readableBytes()];
                buffer.readBytes(values[i]);
                buffer.clear();
            }
            return new MessageObjectCacheReply(message.cacheId, message.ids, values);
        }
    }
}
