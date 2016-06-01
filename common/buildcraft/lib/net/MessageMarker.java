package buildcraft.lib.net;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.core.BCLog;
import buildcraft.lib.BCMessageHandler;
import buildcraft.lib.marker.MarkerCache;
import buildcraft.lib.misc.MessageUtil;

import io.netty.buffer.ByteBuf;

public class MessageMarker implements IMessage {
    private static final boolean DEBUG = BCMessageHandler.DEBUG;

    public boolean add, multiple, connection;
    public int cacheId, count;
    public final List<BlockPos> positions = new ArrayList<>();

    public MessageMarker() {}

    @Override
    public void fromBytes(ByteBuf buf) {
        PacketBuffer packet = new PacketBuffer(buf);
        boolean[] flags = MessageUtil.readBooleanArray(packet, 3);
        add = flags[0];
        multiple = flags[1];
        connection = flags[2];
        cacheId = packet.readShort();
        if (multiple) count = packet.readShort();
        else count = 1;
        for (int i = 0; i < count; i++) {
            positions.add(packet.readBlockPos());
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        count = positions.size();
        multiple = count != 1;
        PacketBuffer packet = new PacketBuffer(buf);
        boolean[] flags = { add, multiple, connection };
        MessageUtil.writeBooleanArray(packet, flags);
        packet.writeShort(cacheId);
        if (multiple) packet.writeShort(count);
        for (int i = 0; i < count; i++) {
            packet.writeBlockPos(positions.get(i));
        }
    }

    @Override
    public String toString() {
        boolean[] flags = { add, multiple, connection };
        return "Message Marker [" + Arrays.toString(flags) + ", cacheId " + cacheId + ", count = " + count + ", positions = " + positions + "]";
    }

    public enum Handler implements IMessageHandler<MessageMarker, IMessage> {
        INSTANCE;

        @Override
        public IMessage onMessage(MessageMarker message, MessageContext ctx) {
            if (ctx.side == Side.CLIENT) {
                handleClient(message, ctx);
            }
            return null;
        }

        @SideOnly(Side.CLIENT)
        private void handleClient(MessageMarker message, MessageContext ctx) {
            World world = Minecraft.getMinecraft().theWorld;
            if (world == null) {
                if (DEBUG) {
                    BCLog.logger.warn("[lib.messages][marker] The world was null for a message!");
                }
                return;
            }
            if (message.cacheId < 0 || message.cacheId >= MarkerCache.CACHES.size()) {
                if (DEBUG) {
                    BCLog.logger.warn("[lib.messages][marker] The cache ID " + message.cacheId + " was invalid!");
                }
                return;
            }
            MarkerCache<?> cache = MarkerCache.CACHES.get(message.cacheId);
            cache.getSubCache(world).handleMessageMain(message);
        }
    }
}
