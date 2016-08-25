package buildcraft.robotics;

import buildcraft.lib.tile.TileBC_Neptune;
import buildcraft.robotics.tile.TileZonePlanner;
import io.netty.buffer.ByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageZonePlannerLayer implements IMessage {
    private BlockPos blockPos;
    private int index;
    private ZonePlan zonePlan;

    public MessageZonePlannerLayer() {
    }

    public MessageZonePlannerLayer(BlockPos blockPos, int index, ZonePlan zonePlan) {
        this.blockPos = blockPos;
        this.index = index;
        this.zonePlan = zonePlan;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        blockPos = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
        index = buf.readInt();
        zonePlan = new ZonePlan().readFromByteBuf(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(blockPos.getX());
        buf.writeInt(blockPos.getY());
        buf.writeInt(blockPos.getZ());
        buf.writeInt(index);
        zonePlan.writeToByteBuf(buf);
    }

    public static enum Handler implements IMessageHandler<MessageZonePlannerLayer, IMessage> {
        INSTANCE;

        @Override
        public IMessage onMessage(MessageZonePlannerLayer message, MessageContext ctx) {
            TileZonePlanner tile = (TileZonePlanner) ctx.getServerHandler().playerEntity.worldObj.getTileEntity(message.blockPos);
            if(tile != null) {
                tile.layers[message.index] = message.zonePlan;
                tile.markDirty();
                tile.sendNetworkUpdate(TileBC_Neptune.NET_RENDER_DATA);
            }
            return null;
        }
    }
}
