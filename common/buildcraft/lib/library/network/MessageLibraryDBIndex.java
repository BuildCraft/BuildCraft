package buildcraft.lib.library.network;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.network.PacketBuffer;

import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import buildcraft.lib.BCLibDatabase;
import buildcraft.lib.library.LibraryEntryHeader;
import buildcraft.lib.library.RemoteLibraryDatabase;

import io.netty.buffer.ByteBuf;

public class MessageLibraryDBIndex implements IMessage {

    private final List<LibraryEntryHeader> headers = new ArrayList<>();

    /** Used by forge internally. DO NOT USE */
    @Deprecated
    public MessageLibraryDBIndex() {}

    public MessageLibraryDBIndex(List<LibraryEntryHeader> headers) {
        this.headers.addAll(headers);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(headers.size());
        PacketBuffer packet = new PacketBuffer(buf);
        for (LibraryEntryHeader header : headers) {
            header.writeToByteBuf(packet);
        }
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        int count = buf.readInt();
        PacketBuffer packet = new PacketBuffer(buf);
        for (int i = 0; i < count; i++) {
            headers.add(new LibraryEntryHeader(packet));
        }
    }

    public enum Handler implements IMessageHandler<MessageLibraryDBIndex, IMessage> {
        INSTANCE;

        @Override
        public IMessage onMessage(MessageLibraryDBIndex message, MessageContext ctx) {
            RemoteLibraryDatabase remote = BCLibDatabase.remoteDB;
            for (LibraryEntryHeader header : message.headers) {
                remote.addNew(header, null);
            }
            return null;
        }
    }
}
