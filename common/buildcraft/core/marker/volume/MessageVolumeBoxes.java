package buildcraft.core.marker.volume;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.buffer.UnpooledDirectByteBuf;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class MessageVolumeBoxes implements IMessage {
    public List<VolumeBox> boxes = new ArrayList<>();

    public MessageVolumeBoxes() {
    }

    public MessageVolumeBoxes(List<VolumeBox> boxes) {
        this.boxes = boxes;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(boxes.size());
        boxes.forEach(box -> box.toBytes(new PacketBuffer(buf)));
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        boxes.clear();
        IntStream.range(0, buf.readInt()).mapToObj(i -> new VolumeBox(new PacketBuffer(buf))).forEach(boxes::add);
    }

    public enum Handler implements IMessageHandler<MessageVolumeBoxes, IMessage> {
        INSTANCE;

        @Override
        public IMessage onMessage(MessageVolumeBoxes message, MessageContext ctx) {
            ClientVolumeBoxes.INSTANCE.boxes.removeIf(box -> !message.boxes.contains(box));
            message.boxes.stream().filter(box -> !ClientVolumeBoxes.INSTANCE.boxes.contains(box)).forEach(ClientVolumeBoxes.INSTANCE.boxes::add);
            for (VolumeBox box : message.boxes) {
                PacketBuffer buf = new PacketBuffer(UnpooledByteBufAllocator.DEFAULT.buffer());
                box.toBytes(buf);
                ClientVolumeBoxes.INSTANCE.boxes.stream().filter(box::equals).findFirst().orElse(null).fromBytes(buf);
            }
            return null;
        }
    }
}
