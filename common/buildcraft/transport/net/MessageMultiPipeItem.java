package buildcraft.transport.net;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nullable;

import io.netty.buffer.ByteBuf;

import net.minecraft.item.EnumDyeColor;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import buildcraft.api.transport.pipe.IPipe;
import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.api.transport.pipe.PipeFlow;

import buildcraft.lib.BCLibProxy;
import buildcraft.lib.misc.MessageUtil;
import buildcraft.lib.net.PacketBufferBC;

import buildcraft.transport.pipe.flow.PipeFlowItems;

public class MessageMultiPipeItem implements IMessage {

    private static final int MAX_ITEMS_PER_PIPE = 10;
    private static final int MAX_POSITIONS = 4000;
    public final Map<BlockPos, List<TravellingItemData>> items = new HashMap<>();

    public MessageMultiPipeItem() {

    }

    @Override
    public void fromBytes(ByteBuf buffer) {
        PacketBufferBC buf = PacketBufferBC.asPacketBufferBc(buffer);
        int blockCount = buf.readShort();
        for (int b = 0; b < blockCount; b++) {
            BlockPos pos = buf.readBlockPos();
            List<TravellingItemData> posItems = new ArrayList<>();
            items.put(pos, posItems);
            int itemCount = buf.readUnsignedByte();
            for (int i = 0; i < itemCount; i++) {
                posItems.add(new TravellingItemData(buf));
            }
        }
    }

    @Override
    public void toBytes(ByteBuf buffer) {
        PacketBufferBC buf = PacketBufferBC.asPacketBufferBc(buffer);
        int blockCount = Math.min(items.size(), MAX_POSITIONS);
        buf.writeShort(blockCount);
        int blockIndex = 0;
        for (Entry<BlockPos, List<TravellingItemData>> entry : items.entrySet()) {
            buf.writeBlockPos(entry.getKey());
            List<TravellingItemData> list = entry.getValue();
            int itemCount = Math.min(list.size(), MAX_ITEMS_PER_PIPE);
            buf.writeByte(itemCount);
            for (int i = 0; i < itemCount; i++) {
                list.get(i).toBuffer(buf);
            }
            if (++blockIndex >= blockCount) {
                break;
            }
        }
    }

    public void append(BlockPos pos, int stackId, byte stackCount, boolean toCenter, EnumFacing side,
        EnumDyeColor colour, byte timeToDest) {
        List<TravellingItemData> list = items.get(pos);
        if (list == null) {
            if (items.size() >= MAX_POSITIONS) {
                return;
            }
            list = new ArrayList<>();
            items.put(pos, list);
        }
        if (list.size() >= MAX_ITEMS_PER_PIPE) {
            return;
        }
        list.add(new TravellingItemData(stackId, stackCount, toCenter, side, colour, timeToDest));
    }

    public static class TravellingItemData {
        public final int stackId;
        public final byte stackCount;
        public final boolean toCenter;
        public final EnumFacing side;
        public final @Nullable EnumDyeColor colour;
        public final byte timeToDest;

        public TravellingItemData(int stackId, byte stackCount, boolean toCenter, EnumFacing side, EnumDyeColor colour,
            byte timeToDest) {
            this.stackId = stackId;
            this.stackCount = stackCount;
            this.toCenter = toCenter;
            this.side = side;
            this.colour = colour;
            this.timeToDest = timeToDest;
        }

        TravellingItemData(PacketBufferBC buf) {
            stackId = buf.readVarInt();
            stackCount = buf.readByte();
            toCenter = buf.readBoolean();
            side = buf.readEnumValue(EnumFacing.class);
            colour = MessageUtil.readEnumOrNull(buf, EnumDyeColor.class);
            timeToDest = buf.readByte();
        }

        void toBuffer(PacketBufferBC buf) {
            buf.writeVarInt(stackId);
            buf.writeByte(stackCount);
            buf.writeBoolean(toCenter);
            buf.writeEnumValue(side);
            MessageUtil.writeEnumOrNull(buf, colour);
            buf.writeByte(timeToDest);
        }
    }

    public static final IMessageHandler<MessageMultiPipeItem, IMessage> HANDLER =
        new IMessageHandler<MessageMultiPipeItem, IMessage>() {

            @Override
            public IMessage onMessage(MessageMultiPipeItem message, MessageContext ctx) {
                World world = BCLibProxy.getProxy().getClientWorld();
                if (world == null) {
                    return null;
                }
                for (Entry<BlockPos, List<TravellingItemData>> entry : message.items.entrySet()) {
                    BlockPos pos = entry.getKey();
                    TileEntity tile = world.getTileEntity(pos);
                    if (tile instanceof IPipeHolder) {
                        IPipe pipe = ((IPipeHolder) tile).getPipe();
                        if (pipe == null) {
                            return null;
                        }
                        PipeFlow flow = pipe.getFlow();
                        if (flow instanceof PipeFlowItems) {
                            ((PipeFlowItems) flow).handleClientReceviedItems(entry.getValue());
                        }
                    }
                }
                return null;
            }
        };
}
