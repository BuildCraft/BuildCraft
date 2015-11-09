package buildcraft.core.lib.network;

import java.util.EnumMap;
import java.util.Map;

import com.google.common.collect.MapMaker;
import com.google.common.collect.Maps;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import net.minecraft.network.PacketBuffer;

import net.minecraftforge.fml.common.network.NetworkRegistry;

import buildcraft.api.core.BCLog;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;

public class ChannelHandlerStats extends ChannelHandler {
    private static final long TIME_GAP = 10000;
    private static boolean showGui = false;
    private static final Map<String, Integer> lengthMap = Maps.newHashMap();

    private static final Map<Class<? extends Packet>, EnumMap<Type, PacketStats>> packetMap = new MapMaker().makeMap();
    // private static Map<Class<? extends Packet>, EnumMap<Type, CompletePacketStats>> completePacketMap = new
    // MapMaker().makeMap();

    @Override
    public void encodeInto(ChannelHandlerContext ctx, Packet packet, ByteBuf data) throws Exception {
        int start = data.writerIndex();
        super.encodeInto(ctx, packet, data);
        int written = data.writerIndex() - start;
        updateInfo(ctx, written, packet.getClass(), Type.WRITE);
    }

    @Override
    public void decodeInto(ChannelHandlerContext ctx, ByteBuf data, Packet packet) {
        int start = data.readerIndex();
        super.decodeInto(ctx, data, packet);
        int read = data.readerIndex() - start;
        updateInfo(ctx, read, packet.getClass(), Type.READ);
    }

    @SuppressWarnings("unchecked")
    private void updateInfo(ChannelHandlerContext ctx, long bytes, Class<? extends Packet> packet, Type type) {
        if (!packetMap.containsKey(packet)) {
            EnumMap<Type, PacketStats> map = Maps.newEnumMap(Type.class);
            map.put(Type.READ, new PacketStats());
            map.put(Type.WRITE, new PacketStats());
            packetMap.put(packet, map);

        }

        String channelName = ctx.channel().attr(NetworkRegistry.FML_CHANNEL).get();
        if (!lengthMap.containsKey(channelName)) {
            lengthMap.put(channelName, new PacketBuffer(Unpooled.buffer()).writeString(channelName).arrayOffset());
        }
        int header = lengthMap.get(channelName);

        EnumMap<Type, PacketStats> map = packetMap.get(packet);
        PacketStats stats = map.get(type);
        stats.packets++;
        stats.bytes += bytes + header;

        long now = System.currentTimeMillis();
        long diff = now - stats.lastStatTime;
        if (diff > TIME_GAP && !showGui) {
            stats.lastStatTime = now;

            BCLog.logger.info("Over " + diff + "ms, " + type.operation + " " + stats.bytes + " bytes " + type.word + " " + stats.packets
                + " packets for " + packet.getName());
            stats.bytes = 0;
            stats.packets = 0;
        }

        if (packet != Packet.class) {
            Class<?> superClass = packet.getSuperclass();
            updateInfo(ctx, bytes, (Class<? extends Packet>) superClass, type);
        }
    }

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

    private static class PacketSource {
        private final String className, extraInfo;
        private final transient int hashCode;

        public PacketSource(Class<? extends Packet> packet, String extraData) {
            if (packet == null)
                throw new NullPointerException("packet");
            if (extraData == null)
                throw new NullPointerException("extraData");

            className = packet.getName();
            this.extraInfo = extraData;

            /* Just compute the hash code once instead of every time- this will speed up hash code computation when this
             * is in a hash map */
            HashCodeBuilder hcb = new HashCodeBuilder();
            hcb.append(className);
            hcb.append(extraInfo);
            hashCode = hcb.toHashCode();
        }

        @Override
        public int hashCode() {
            return hashCode;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            PacketSource other = (PacketSource) obj;
            EqualsBuilder eb = new EqualsBuilder();
            eb.append(className, other.className);
            eb.append(extraInfo, other.extraInfo);
            return eb.isEquals();
        }
    }
}
