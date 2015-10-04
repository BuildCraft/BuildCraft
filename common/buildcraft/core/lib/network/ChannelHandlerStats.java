package buildcraft.core.lib.network;

import java.util.EnumMap;
import java.util.Map;

import com.google.common.collect.MapMaker;

import buildcraft.api.core.BCLog;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public class ChannelHandlerStats extends ChannelHandler {
    private enum Type {
        READ("read", "from"),
        WRITE("wrote", "to");

        final String operation, word;

        private Type(String operation, String word) {
            this.operation = operation;
            this.word = word;
        }
    }

    private static class PacketStats {
        private int packets = 0;
        private long bytes = 0;
        private long lastStatTime;

        private PacketStats() {
            lastStatTime = System.currentTimeMillis();
        }
    }

    private static final long TIME_GAP = 10000;

    private static Map<Class<? extends Packet>, EnumMap<Type, PacketStats>> packetMap = new MapMaker().makeMap();

    @Override
    public void encodeInto(ChannelHandlerContext ctx, Packet packet, ByteBuf data) throws Exception {
        int start = data.writerIndex();
        super.encodeInto(ctx, packet, data);
        int written = data.writerIndex() - start;
        updateInfo(written, packet.getClass(), Type.WRITE);
    }

    @Override
    public void decodeInto(ChannelHandlerContext ctx, ByteBuf data, Packet packet) {
        int start = data.readerIndex();
        super.decodeInto(ctx, data, packet);
        int read = data.readerIndex() - start;
        updateInfo(read, packet.getClass(), Type.READ);
    }

    @SuppressWarnings("unchecked")
    private void updateInfo(long bytes, Class<? extends Packet> packet, Type type) {
        if (!packetMap.containsKey(packet)) {
            EnumMap<Type, PacketStats> map = new EnumMap<Type, PacketStats>(Type.class);
            map.put(Type.READ, new PacketStats());
            map.put(Type.WRITE, new PacketStats());
            packetMap.put(packet, map);

        }
        EnumMap<Type, PacketStats> map = packetMap.get(packet);
        PacketStats stats = map.get(type);
        stats.packets++;
        stats.bytes += bytes;

        long now = System.currentTimeMillis();
        long diff = now - stats.lastStatTime;
        if (diff > TIME_GAP) {
            stats.lastStatTime = now;

            BCLog.logger.info("Over " + diff + "ms, " + type.operation + " " + stats.bytes + " bytes " + type.word + " " + stats.packets
                + " packets for " + packet.getName());
            stats.bytes = 0;
            stats.packets = 0;
        }

        if (packet != Packet.class) {
            Class<?> superClass = packet.getSuperclass();
            updateInfo(bytes, (Class<? extends Packet>) superClass, type);
        }
    }
}
