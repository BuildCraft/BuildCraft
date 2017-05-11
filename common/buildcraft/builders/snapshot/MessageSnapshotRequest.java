package buildcraft.builders.snapshot;

import buildcraft.lib.net.PacketBufferBC;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class MessageSnapshotRequest implements IMessage {
    private Snapshot.Header header;

    public MessageSnapshotRequest() {
    }

    public MessageSnapshotRequest(Snapshot.Header header) {
        this.header = header;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        header.writeToByteBuf(new PacketBufferBC(buf));
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        header = new Snapshot.Header();
        header.readFromByteBuf(new PacketBufferBC(buf));
    }

    public static final IMessageHandler<MessageSnapshotRequest, MessageSnapshotResponse> HANDLER =
            (MessageSnapshotRequest message, MessageContext ctx) -> {
                Snapshot snapshot = GlobalSavedDataSnapshots.get(Side.SERVER).getSnapshotByHeader(message.header);
                return snapshot != null ? new MessageSnapshotResponse(snapshot) : null;
            };
}
