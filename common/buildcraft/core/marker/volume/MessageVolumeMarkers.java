package buildcraft.core.marker.volume;

import buildcraft.lib.misc.data.Box;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class MessageVolumeMarkers implements IMessage {
    public List<VolumeBox> boxes = new ArrayList<>();

    public MessageVolumeMarkers() {
    }

    public MessageVolumeMarkers(List<VolumeBox> boxes) {
        this.boxes = boxes;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(boxes.size());
        boxes.forEach(box -> {
            box.box.writeData(new PacketBuffer(buf));
            buf.writeBoolean(box.player != null);
            if (box.player != null) {
                new PacketBuffer(buf).writeString(box.player);
            }
        });
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        boxes.clear();
        IntStream.range(0, buf.readInt()).mapToObj(i -> {
            Box box = new Box();
            box.readData(new PacketBuffer(buf));
            return new VolumeBox(box, buf.readBoolean() ? new PacketBuffer(buf).readString(1024) : null);
        }).forEach(boxes::add);
    }

    public enum Handler implements IMessageHandler<MessageVolumeMarkers, IMessage> {
        INSTANCE;

        @Override
        public IMessage onMessage(MessageVolumeMarkers message, MessageContext ctx) {
            ClientVolumeMarkers.INSTANCE.boxes = message.boxes;
            return null;
        }
    }
}
