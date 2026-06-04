// STUB(R.Chen): Forge IMessageHandler — compile shim.
package net.minecraftforge.fml.common.network.simpleimpl;

public interface IMessageHandler<REQ extends IMessage, REPLY extends IMessage> {
    REPLY onMessage(REQ message, MessageContext ctx);
}
