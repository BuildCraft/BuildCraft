package buildcraft.lib.net;

import java.io.IOException;

import com.google.common.base.Throwables;

import net.minecraft.entity.Entity;
import net.minecraft.inventory.Container;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;

import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import buildcraft.lib.gui.ContainerBC8;
import buildcraft.lib.net.command.*;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/** Sending and receiving misc commands (any data) from {@link TileEntity}, {@link Entity}, or {@link Container} between
 * the server and client. Uses a single string for command type so this is not recommended for every-second messages. */
// Deprecated as commands use strings :/
@Deprecated
public class MessageCommand implements IMessage {

    private ICommandTarget target;
    private String name;
    private final IPayloadWriter writer;
    private PacketBuffer payload;

    /** Used by forge to construct this upon receive. Do not use! */
    @Deprecated
    public MessageCommand() {
        writer = null;
    }

    private MessageCommand(ICommandTarget target, String name, IPayloadWriter writer) {
        this.target = target;
        this.name = name;
        this.writer = writer;
    }

    public MessageCommand(TileEntity sender, String name, IPayloadWriter writer) {
        this(CommandTargets.getForTile(sender), name, writer);
    }

    public MessageCommand(Entity sender, String name, IPayloadWriter writer) {
        this(CommandTargets.getForEntity(sender), name, writer);
    }

    public MessageCommand(ContainerBC8 container, String name, IPayloadWriter writer) {
        this(CommandTargets.getForContainer(container), name, writer);
    }

    // Packet breakdown:
    // STRING - NAME
    // BYTE - TYPE
    // USHORT - PAYLOAD_SIZE->"size"
    // BYTE[size] - PAYLOAD

    // Payload breakdown:
    // BYTE[?] - TARGET_INFO
    // BYTE[?] COMMAND_DATA

    @Override
    public void fromBytes(ByteBuf buf) {
        PacketBuffer buffer = new PacketBuffer(buf);
        name = buffer.readStringFromBuffer(24);
        byte type = buffer.readByte();
        if (type == CommandTargetType.TILE.ordinal()) {
            target = CommandTargets.TARGET_TILE;
        } else if (type == CommandTargetType.ENTITY.ordinal()) {
            target = CommandTargets.TARGET_ENTITY;
        } else if (type == CommandTargetType.CONTAINER.ordinal()) {
            target = CommandTargets.TARGET_CONTAINER;
        }
        int payloadSize = buffer.readUnsignedShort();
        ByteBuf read = buffer.readBytes(payloadSize);
        payload = new PacketBuffer(read);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        payload = new PacketBuffer(Unpooled.buffer());
        target.writePositionData(payload);
        writer.write(payload);

        PacketBuffer buffer = new PacketBuffer(buf);
        buffer.writeString(name);
        buffer.writeByte(target.getType().ordinal());
        buffer.writeShort(payload.readableBytes());
        buffer.writeBytes(payload, payload.readableBytes());
    }

    public static enum Handler implements IMessageHandler<MessageCommand, MessageCommand> {
        INSTANCE;

        @Override
        public MessageCommand onMessage(MessageCommand message, MessageContext ctx) {
            ICommandReceiver rec = message.target.getReceiver(message.payload, ctx);
            if (rec == null) return null;
            try {
                return rec.receiveCommand(message.name, ctx.side, message.payload);
            } catch (IOException e) {
                throw Throwables.propagate(e);
            }
        }
    }
}
