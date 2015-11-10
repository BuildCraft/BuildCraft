package buildcraft.core.lib.network.base;

import net.minecraft.network.INetHandler;

import net.minecraftforge.fml.common.network.FMLIndexedMessageToMessageCodec;
import net.minecraftforge.fml.common.network.NetworkRegistry;

import buildcraft.core.lib.network.PacketEntityUpdate;
import buildcraft.core.lib.network.PacketGuiReturn;
import buildcraft.core.lib.network.PacketGuiWidget;
import buildcraft.core.lib.network.PacketSlotChange;
import buildcraft.core.lib.network.PacketTileState;
import buildcraft.core.lib.network.PacketTileUpdate;
import buildcraft.core.lib.network.base.NetworkStatRecorder.EnumOpType;
import buildcraft.core.lib.network.command.PacketCommand;
import buildcraft.core.proxy.CoreProxy;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public final class ChannelHandler extends FMLIndexedMessageToMessageCodec<Packet> {
    private static boolean recordStats = false;
    private int index = 0;

    public ChannelHandler() {
        // Packets common to buildcraft.core.network
        registerPacketType(PacketTileUpdate.class);
        registerPacketType(PacketTileState.class);
        registerPacketType(PacketSlotChange.class);
        registerPacketType(PacketGuiReturn.class);
        registerPacketType(PacketGuiWidget.class);
        registerPacketType(PacketCommand.class);
        registerPacketType(PacketEntityUpdate.class);
    }

    public void registerPacketType(Class<? extends Packet> type) {
        super.addDiscriminator(index++, type);
    }

    public static boolean shouldRecordStats() {
        return recordStats;
    }

    public static void setRecordStats(boolean newValue) {
        if (newValue != recordStats) {
            if (newValue) StatisticsFrame.createStatisticsFrame();
            else StatisticsFrame.destroyStatisticsFrame();
        }
        recordStats = newValue;
    }

    @Override
    public ChannelHandler addDiscriminator(int discriminator, Class<? extends Packet> type) {
        throw new IllegalArgumentException("Use registerPacketType instead!");
    }

    @Override
    public void encodeInto(ChannelHandlerContext ctx, Packet packet, ByteBuf data) throws Exception {
        int start = data.writerIndex();

        packet.writeData(data);

        int written = data.writerIndex() - start;
        recordStat(ctx, written, packet, EnumOpType.WRITE);
    }

    @Override
    public void decodeInto(ChannelHandlerContext ctx, ByteBuf data, Packet packet) {
        int start = data.readerIndex();

        INetHandler handler = ctx.channel().attr(NetworkRegistry.NET_HANDLER).get();
        packet.readData(data);
        packet.player = CoreProxy.proxy.getPlayerFromNetHandler(handler);

        int read = data.readerIndex() - start;
        recordStat(ctx, read, packet, EnumOpType.READ);
    }

    private void recordStat(ChannelHandlerContext ctx, long bytes, Packet packet, EnumOpType type) {
        if (shouldRecordStats()) {
            NetworkStatRecorder.recordStat(ctx, bytes, packet, type);
        }
    }
}
