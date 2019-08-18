package buildcraft.transport.net;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import io.netty.buffer.ByteBuf;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.api.transport.pipe.PipeFlow;

import buildcraft.lib.BCLibProxy;
import buildcraft.lib.net.PacketBufferBC;

import buildcraft.transport.pipe.flow.PipeFlowFluids;

public class MessageMultiPipeFluid implements IMessage {

    private static final int MAX_POSITIONS = 4000;
    public final Map<BlockPos, FluidFlowData> fluids = new HashMap<>();

    public MessageMultiPipeFluid() {

    }

    @Override
    public void fromBytes(ByteBuf buffer) {
        PacketBufferBC buf = PacketBufferBC.asPacketBufferBc(buffer);
        int blockCount = buf.readShort();
        for (int b = 0; b < blockCount; b++) {
            BlockPos pos = buf.readBlockPos();
            fluids.put(pos, new FluidFlowData(buf));
        }
    }

    @Override
    public void toBytes(ByteBuf buffer) {
        PacketBufferBC buf = PacketBufferBC.asPacketBufferBc(buffer);
        int blockCount = Math.min(fluids.size(), MAX_POSITIONS);
        buf.writeShort(blockCount);
        int blockIndex = 0;
        for (Entry<BlockPos, FluidFlowData> entry : fluids.entrySet()) {
            buf.writeBlockPos(entry.getKey());
            entry.getValue().toBuffer(buf);
            if (++blockIndex > blockCount) {
                throw new IllegalStateException("Too many blocks: " + blockIndex);
            }
        }
    }

    public boolean append(BlockPos pos, short[] amounts, byte[] dirs) {
        if (fluids.size() >= MAX_POSITIONS) {
            return false;
        }
        fluids.put(pos, new FluidFlowData(amounts, dirs));
        return true;
    }

    public static class FluidFlowData {
        public final short[] amounts;
        public final byte[] dirs;

        public FluidFlowData(short[] amounts, byte[] dirs) {
            assert amounts.length == 7;
            assert dirs.length == 7;
            this.amounts = amounts;
            this.dirs = dirs;
        }

        FluidFlowData(PacketBufferBC buf) {
            amounts = new short[7];
            for (int i = 0; i < 7; i++) {
                amounts[i] = buf.readShort();
            }
            dirs = new byte[7];
            for (int i = 0; i < 7; i++) {
                dirs[i] = (byte) buf.readFixedBits(2);
            }
        }

        void toBuffer(PacketBufferBC buf) {
            for (int i = 0; i < 7; i++) {
                buf.writeShort(amounts[i]);
            }
            for (int i = 0; i < 7; i++) {
                buf.writeFixedBits(dirs[i], 2);
            }
        }
    }

    public static final IMessageHandler<MessageMultiPipeFluid, IMessage> HANDLER = new IMessageHandler<
        MessageMultiPipeFluid, IMessage>() {

        @Override
        public IMessage onMessage(MessageMultiPipeFluid message, MessageContext ctx) {
            World world = BCLibProxy.getProxy().getClientWorld();
            if (world == null) {
                return null;
            }
            for (Entry<BlockPos, FluidFlowData> entry : message.fluids.entrySet()) {
                BlockPos pos = entry.getKey();
                TileEntity tile = world.getTileEntity(pos);
                if (tile instanceof IPipeHolder) {
                    PipeFlow flow = ((IPipeHolder) tile).getPipe().getFlow();
                    if (flow instanceof PipeFlowFluids) {
                        ((PipeFlowFluids) flow).handleClientReceviedFlow(entry.getValue());
                    }
                }
            }
            return null;
        }
    };
}
