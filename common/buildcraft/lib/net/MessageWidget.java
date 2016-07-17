package buildcraft.lib.net;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;

import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import buildcraft.lib.LibProxy;
import buildcraft.lib.gui.ContainerBC_Neptune;
import buildcraft.lib.net.command.IPayloadWriter;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/** Specialised version of {@link MessageCommand} that deals only with widgets */
public class MessageWidget implements IMessage {

    private int windowId, widgetId;
    private PacketBuffer payload;

    /** Used by forge to construct this upon receive. Do not use! */
    @Deprecated
    public MessageWidget() {}

    public MessageWidget(int windowId, int widgetId, IPayloadWriter writer) {
        this.windowId = windowId;
        this.widgetId = widgetId;
        this.payload = new PacketBuffer(Unpooled.buffer());
        writer.write(payload);
    }

    // Packet breakdown:
    // INT - WindowId
    // INT - WidgetId
    // USHORT - PAYLOAD_SIZE->"size"
    // BYTE[size] - PAYLOAD

    @Override
    public void fromBytes(ByteBuf buf) {
        windowId = buf.readInt();
        widgetId = buf.readInt();
        int payloadSize = buf.readUnsignedShort();
        ByteBuf read = buf.readBytes(payloadSize);
        payload = new PacketBuffer(read);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(windowId);
        buf.writeInt(widgetId);
        buf.writeShort(payload.readableBytes());
        buf.writeBytes(payload);
    }

    public enum Handler implements IMessageHandler<MessageWidget, MessageWidget> {
        INSTANCE;

        @Override
        public MessageWidget onMessage(MessageWidget message, MessageContext ctx) {
            int windowId = message.windowId;
            int widgetId = message.widgetId;
            EntityPlayer player = LibProxy.getProxy().getPlayerForContext(ctx);
            if (player != null && player.openContainer instanceof ContainerBC_Neptune && player.openContainer.windowId == windowId) {
                ContainerBC_Neptune container = (ContainerBC_Neptune) player.openContainer;
                container.handleWidgetMessage(ctx, widgetId, message.payload, ctx.side);
            }
            return null;
        }
    }
}
