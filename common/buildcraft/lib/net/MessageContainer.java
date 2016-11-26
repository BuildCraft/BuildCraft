package buildcraft.lib.net;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;

import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

import buildcraft.lib.BCLibProxy;
import buildcraft.lib.gui.ContainerBC_Neptune;
import buildcraft.lib.misc.MessageUtil;
import buildcraft.lib.net.command.IPayloadWriter;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/** Specialised version of {@link MessageCommand} that deals only with containers for gui's. */
public class MessageContainer implements IMessage {

    private int windowId;
    private PacketBufferBC payload;

    /** Used by forge to construct this upon receive. Do not use! */
    @Deprecated
    public MessageContainer() {}

    public MessageContainer(ContainerBC_Neptune container, IPayloadWriter writer) {
        this(container.windowId, writer);
    }

    public MessageContainer(int windowId, IPayloadWriter writer) {
        this.windowId = windowId;
        this.payload = new PacketBufferBC(Unpooled.buffer());
        writer.write(payload);
    }

    // Packet breakdown:
    // INT - WindowId
    // USHORT - PAYLOAD_SIZE->"size"
    // BYTE[size] - PAYLOAD

    @Override
    public void fromBytes(ByteBuf buf) {
        windowId = buf.readInt();
        int payloadSize = buf.readUnsignedShort();
        ByteBuf read = buf.readBytes(payloadSize);
        payload = new PacketBufferBC(read);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(windowId);
        int length = payload.readableBytes();
        buf.writeShort(length);
        buf.writeBytes(payload, 0, length);
    }

    public enum Handler implements IMessageHandler<MessageContainer, IMessage> {
        INSTANCE;

        @Override
        public IMessage onMessage(MessageContainer message, MessageContext ctx) {
            try {
                int windowId = message.windowId;
                EntityPlayer player = BCLibProxy.getProxy().getPlayerForContext(ctx);
                if (player != null && player.openContainer instanceof ContainerBC_Neptune && player.openContainer.windowId == windowId) {
                    ContainerBC_Neptune container = (ContainerBC_Neptune) player.openContainer;
                    container.handleMessage(ctx, message.payload, ctx.side);
                    MessageUtil.ensureEmpty(message.payload, ctx.side == Side.CLIENT, getClass().getSimpleName());
                }
                return null;
            } catch (IOException e) {
                throw new Error(e);
            }
        }
    }
}
