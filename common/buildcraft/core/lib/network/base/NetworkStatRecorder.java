package buildcraft.core.lib.network.base;

import java.util.EnumMap;
import java.util.Map;
import com.google.common.collect.MapMaker;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;

import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.common.network.NetworkRegistry;

import buildcraft.api.core.ISerializable;
import buildcraft.core.lib.network.PacketCoordinates;
import buildcraft.core.lib.network.PacketUpdate;
import buildcraft.core.lib.network.command.PacketCommand;

public class NetworkStatRecorder {
    static final Map<String, Integer> lengthMap = Maps.newHashMap();
    static final Map<PacketSource, EnumMap<EnumOpType, PacketStats>> packetMap = new MapMaker().makeMap();

    public static void recordStat(ChannelHandlerContext ctx, long bytes, Packet packet, EnumOpType type) {
        PacketSource source = new PacketSource(packet);
        if (!packetMap.containsKey(source)) {
            EnumMap<EnumOpType, PacketStats> map = Maps.newEnumMap(EnumOpType.class);
            map.put(EnumOpType.READ, new PacketStats());
            map.put(EnumOpType.WRITE, new PacketStats());
            packetMap.put(source, map);

        }

        String channelName = ctx.channel().attr(NetworkRegistry.FML_CHANNEL).get();
        if (!lengthMap.containsKey(channelName)) {
            lengthMap.put(channelName, new PacketBuffer(Unpooled.buffer()).writeString(channelName).arrayOffset());
        }
        int header = lengthMap.get(channelName);

        EnumMap<EnumOpType, PacketStats> map = packetMap.get(source);
        map.get(type).increment(bytes + header);
    }

    enum EnumOpType {
        READ("read", "from"),
        WRITE("wrote", "to");

        final String operation, word;

        private EnumOpType(String operation, String word) {
            this.operation = operation;
            this.word = word;
        }
    }

    static class PacketStats {
        public static final int HISTORY_SIZE = 120;
        public static final long HISTORY_GAP = 1000;

        final int[] packets = new int[HISTORY_SIZE];
        final long[] bytes = new long[HISTORY_SIZE];
        final long[] statTime = new long[HISTORY_SIZE];
        int currentIndex = 0;

        private PacketStats() {
            statTime[currentIndex] = System.currentTimeMillis();
        }

        public void increment(long bytes) {
            this.packets[currentIndex]++;
            this.bytes[currentIndex] += bytes;
            process();
        }

        private int incrementIndex() {
            currentIndex++;
            if (currentIndex >= HISTORY_SIZE) {
                currentIndex = 0;
            }
            return currentIndex;
        }

        public int getLastIndex() {
            if (currentIndex - 1 < 0) {
                return HISTORY_SIZE - 1;
            }
            return currentIndex - 1;
        }

        private void process() {
            long now = System.currentTimeMillis();
            if (now - statTime[currentIndex] < HISTORY_GAP) {
                return;
            }
            statTime[incrementIndex()] = now;
            packets[currentIndex] = 0;
            bytes[currentIndex] = 0;
            StatisticsFrame.update();
        }
    }

    static class PacketSource {
        final Class<? extends Packet> clazz;
        final String className, extraInfo;
        private final transient int hashCode;

        public static String extraInfo(Packet packet) {
            if (packet instanceof PacketCoordinates) {
                TileEntity tile = ((PacketCoordinates) packet).tile;
                return tile == null ? "generic" : tile.getClass().getName();
            } else if (packet instanceof PacketUpdate) {
                ISerializable ser = ((PacketUpdate) packet).payload;
                return ser == null ? "generic" : ser.getClass().getName();
            } else if (packet instanceof PacketCommand) {
                return ((PacketCommand) packet).command;
            }
            return null;
        }

        public PacketSource(Packet packet) {
            this(packet.getClass(), extraInfo(packet));
        }

        public PacketSource(Class<? extends Packet> packet, String extraInfo) {
            if (packet == null) throw new NullPointerException("packet");
            if (extraInfo == null) extraInfo = "unknown";

            this.clazz = packet;
            this.className = packet.getName();
            this.extraInfo = extraInfo;

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
