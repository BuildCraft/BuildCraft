package buildcraft.lib.net.cache;

import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import io.netty.buffer.ByteBuf;

public class MessageObjectCacheReply implements IMessage {

    private int cacheId;

    private int[] ids;
    private byte[][] values;

    MessageObjectCacheReply(int cacheId, int[] ids, byte[][] values) {
        this.cacheId = cacheId;
        this.ids = ids;
        this.values = values;
    }

    @Override
    public void toBytes(ByteBuf buf) {

        throw new AbstractMethodError("// TODO: Implement this!");
    }

    @Override
    public void fromBytes(ByteBuf buf) {

        throw new AbstractMethodError("// TODO: Implement this!");
    }

    public enum Handler implements IMessageHandler<MessageObjectCacheReply, IMessage> {
        INSTANCE;

        @Override
        public IMessage onMessage(MessageObjectCacheReply message, MessageContext ctx) {
            // TODO Auto-generated method stub
            throw new AbstractMethodError("// TODO: Implement this!");
        }
    }

}
