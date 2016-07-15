package buildcraft.robotics;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.function.Consumer;

public class MessageZonePlannerMapChunkResponse implements IMessage {
    private int chunkX;
    private int chunkZ;
    private ZonePlannerMapChunk data;

    public MessageZonePlannerMapChunkResponse() {
    }

    public MessageZonePlannerMapChunkResponse(int chunkX, int chunkZ, ZonePlannerMapChunk data) {
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.data = data;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        chunkX = buf.readInt();
        chunkZ = buf.readInt();
        int size = buf.readInt();
        data = new ZonePlannerMapChunk();
        for(int i = 0; i < size; i++) {
            BlockPos pos = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
            int colors = buf.readInt();
            data.data.put(pos, colors);
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(chunkX);
        buf.writeInt(chunkZ);
        buf.writeInt(data.data.size());
        for(BlockPos pos : data.data.keySet()) {
            int color = data.data.get(pos);
            buf.writeInt(pos.getX());
            buf.writeInt(pos.getY());
            buf.writeInt(pos.getZ());
            buf.writeInt(color);
        }
    }

    public static class Handler implements IMessageHandler<MessageZonePlannerMapChunkResponse, IMessage> {
        @Override
        public IMessage onMessage(MessageZonePlannerMapChunkResponse message, MessageContext ctx) {
            Minecraft.getMinecraft().addScheduledTask(() -> {
                for(Pair<Pair<Integer, Integer>, Consumer<ZonePlannerMapChunk>> pendingRequest : new ArrayList<>(ZonePlannerMapDataClient.instance.pendingRequests)) {
                    Pair<Integer, Integer> chunkPosPair = pendingRequest.getLeft();
                    Consumer<ZonePlannerMapChunk> consumer = pendingRequest.getRight();
                    if(chunkPosPair.equals(Pair.of(message.chunkX, message.chunkZ))) {
                        consumer.accept(message.data);
                    }
                }
            });
            return null;
        }
    }
}
