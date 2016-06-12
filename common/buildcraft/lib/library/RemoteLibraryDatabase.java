package buildcraft.lib.library;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import buildcraft.lib.BCMessageHandler;
import buildcraft.lib.library.network.MessageLibraryRequest;

public class RemoteLibraryDatabase extends LibraryDatabase_Neptune {
    private final Set<LibraryEntryHeader> headers = new HashSet<>();

    @Override
    public boolean addNew(LibraryEntryHeader header, LibraryEntryData data) {
        return headers.add(header);
    }

    @Override
    public Collection<LibraryEntryHeader> getAllHeaders() {
        return headers;
    }

    public static void requestIndex() {
        MessageLibraryRequest message = new MessageLibraryRequest();
        BCMessageHandler.netWrapper.sendToServer(message);
    }

    public static void requestEntry(LibraryEntryHeader header) {
        MessageLibraryRequest message = new MessageLibraryRequest(header);
        BCMessageHandler.netWrapper.sendToServer(message);
    }
}
