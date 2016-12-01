package buildcraft.lib.net;

import java.io.IOException;

import com.google.common.base.Throwables;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import buildcraft.lib.BCLibProxy;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class MessageUpdateTile implements IMessage {
    private BlockPos pos;
    private PacketBufferBC payload;

    /** Used by forge to construct this upon receive. Do not use! */
    @Deprecated
    public MessageUpdateTile() {}

    public MessageUpdateTile(BlockPos pos, IPayloadWriter writer) {
        this.pos = pos;
        payload = new PacketBufferBC(Unpooled.buffer());
        writer.write(payload);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        PacketBuffer buffer = new PacketBuffer(buf);
        this.pos = buffer.readBlockPos();
        int size = buffer.readUnsignedShort();
        payload = new PacketBufferBC(buffer.readBytes(size));
    }

    @Override
    public void toBytes(ByteBuf buf) {
        PacketBuffer buffer = new PacketBuffer(buf);
        buffer.writeBlockPos(pos);
        int length = payload.readableBytes();
        buffer.writeShort(length);
        buffer.writeBytes(payload, 0, length);
    }

    public enum Handler implements IMessageHandler<MessageUpdateTile, IMessage> {
        INSTANCE;

        @Override
        public IMessage onMessage(final MessageUpdateTile message, MessageContext ctx) {
            EntityPlayer player = BCLibProxy.getProxy().getPlayerForContext(ctx);
            if (player == null || player.world == null) return null;
            TileEntity tile = player.world.getTileEntity(message.pos);
            if (tile instanceof IPayloadReceiver) {
                try {
                    return ((IPayloadReceiver) tile).receivePayload(ctx, message.payload);
                } catch (IOException io) {
                    throw Throwables.propagate(io);
                }
            }
            return null;
        }
    }
}
