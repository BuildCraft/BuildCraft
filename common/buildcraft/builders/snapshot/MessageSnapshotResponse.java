package buildcraft.builders.snapshot;

import buildcraft.lib.nbt.NbtSquisher;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;

import java.io.IOException;

public class MessageSnapshotResponse implements IMessage {
    private Snapshot snapshot;

    public MessageSnapshotResponse() {
    }

    public MessageSnapshotResponse(Snapshot snapshot) {
        this.snapshot = snapshot;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        byte[] bytes = NbtSquisher.squishBuildCraftV1(Snapshot.writeToNBT(snapshot));
        buf.writeInt(bytes.length);
        buf.writeBytes(bytes);
//        try {
//            CompressedStreamTools.write(Snapshot.writeToNBT(snapshot), new ByteBufOutputStream(buf));
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        try {
            snapshot = Snapshot.readFromNBT(NbtSquisher.expand(buf.readBytes(buf.readInt()).array()));
//            snapshot = Snapshot.readFromNBT(CompressedStreamTools.read(new ByteBufInputStream(buf), NBTSizeTracker.INFINITE));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static final IMessageHandler<MessageSnapshotResponse, IMessage> HANDLER = (message, ctx) -> {
        ClientSnapshots.INSTANCE.onSnapshotReceived(message.snapshot);
        return null;
    };
}
