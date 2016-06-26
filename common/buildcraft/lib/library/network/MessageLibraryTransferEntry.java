package buildcraft.lib.library.network;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map.Entry;

import net.minecraft.network.PacketBuffer;

import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import buildcraft.lib.BCLibDatabase;
import buildcraft.lib.library.*;

import io.netty.buffer.ByteBuf;

public class MessageLibraryTransferEntry implements IMessage {
    private LibraryEntryHeader header;
    private ILibraryEntryData data;

    /** Constructor used by forge- do not use! */
    @Deprecated
    public MessageLibraryTransferEntry() {}

    public MessageLibraryTransferEntry(LibraryEntryHeader header, ILibraryEntryData data) {
        this.header = header;
        this.data = data;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        try {
            PacketBuffer packet = new PacketBuffer(buf);
            int length = packet.readInt();
            byte[] bytes = new byte[length];
            packet.readBytes(bytes);
            ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
            Entry<LibraryEntryHeader, ILibraryEntryData> entry = LibraryDatabase_Neptune.load(bais);
            header = entry.getKey();
            data = entry.getValue();
        } catch (IOException io) {
            throw new IllegalArgumentException(io);
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        PacketBuffer packet = new PacketBuffer(buf);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        LibraryDatabase_Neptune.save(baos, header, data);
        byte[] bytes = baos.toByteArray();
        packet.writeInt(bytes.length);
        packet.writeBytes(bytes);
    }

    public enum Handler implements IMessageHandler<MessageLibraryTransferEntry, IMessage> {
        INSTANCE;

        @Override
        public IMessage onMessage(MessageLibraryTransferEntry message, MessageContext ctx) {
            LocalLibraryDatabase local = BCLibDatabase.LOCAL_DB;
            local.addNew(message.header, message.data);

            RemoteLibraryDatabase remote = BCLibDatabase.remoteDB;
            if (remote != null) {
                remote.addNew(message.header, message.data);
            }

            BCLibDatabase.fillEntries();

            return null;
        }
    }
}
