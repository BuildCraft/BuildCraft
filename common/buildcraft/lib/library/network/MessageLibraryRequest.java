package buildcraft.lib.library.network;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.network.PacketBuffer;

import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import buildcraft.lib.BCLibDatabase;
import buildcraft.lib.library.LibraryEntryData;
import buildcraft.lib.library.LibraryEntryHeader;
import buildcraft.lib.library.LocalLibraryDatabase;

import io.netty.buffer.ByteBuf;

public class MessageLibraryRequest implements IMessage {
    public static final int ID_REQUEST_INDEX = 0;
    public static final int ID_REQUEST_SINGLE = 1;

    private int id;
    private LibraryEntryHeader header;

    public MessageLibraryRequest() {
        id = ID_REQUEST_INDEX;
    }

    public MessageLibraryRequest(LibraryEntryHeader headerWanted) {
        id = ID_REQUEST_SINGLE;
        header = headerWanted;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeByte(id);
        if (id == ID_REQUEST_SINGLE) {
            header.writeToByteBuf(new PacketBuffer(buf));
        }
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        id = buf.readByte();
        if (id == ID_REQUEST_SINGLE) {
            header = new LibraryEntryHeader(new PacketBuffer(buf));
        }
    }

    public enum Handler implements IMessageHandler<MessageLibraryRequest, IMessage> {
        INSTANCE;

        @Override
        public IMessage onMessage(MessageLibraryRequest message, MessageContext ctx) {
            LocalLibraryDatabase local = BCLibDatabase.LOCAL_DB;
            if (message.id == ID_REQUEST_SINGLE) {
                LibraryEntryData data = local.getEntry(message.header);
                if (data != null) {
                    MessageLibraryTransferEntry ret = new MessageLibraryTransferEntry(message.header, data);
                    return ret;
                }
            } else if (message.id == ID_REQUEST_INDEX) {
                List<LibraryEntryHeader> headers = new ArrayList<>(local.getAllHeaders());
                return new MessageLibraryDBIndex(headers);
            }
            return null;
        }
    }
}
