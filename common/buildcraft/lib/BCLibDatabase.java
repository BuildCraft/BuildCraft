package buildcraft.lib;

import java.util.*;

import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientConnectedToServerEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.core.BCLog;
import buildcraft.lib.library.LibraryEntryHeader;
import buildcraft.lib.library.LibraryEntryType;
import buildcraft.lib.library.LocalLibraryDatabase;
import buildcraft.lib.library.RemoteLibraryDatabase;
import buildcraft.lib.library.book.LibraryEntryBook;
import buildcraft.lib.misc.MessageUtil;
import buildcraft.lib.misc.WorkerThreadUtil;

public class BCLibDatabase {
    public static final Map<String, LibraryEntryType> REGISTERED_TYPES = new HashMap<>();
    public static final LocalLibraryDatabase LOCAL_DB = new LocalLibraryDatabase();
    public static RemoteLibraryDatabase remoteDB = null;
    public static final List<LibraryEntryHeader> allEntries = new ArrayList<>();

    static {
        REGISTERED_TYPES.put(LibraryEntryBook.KIND, LibraryEntryBook::new);
    }

    public static void fmlInit() {
        WorkerThreadUtil.executeDependantTask(LOCAL_DB::readAll);
    }

    public static void onServerStarted() {
        LOCAL_DB.onServerStarted();
        fillEntries();
    }

    @SideOnly(Side.CLIENT)
    public static void connectToServer(ClientConnectedToServerEvent event) {
        BCLog.logger.info("Connected to a server!");
        if (FMLCommonHandler.instance().getMinecraftServerInstance() != null) {
            remoteDB = null;
            fillEntries();
            return;
        }
        remoteDB = new RemoteLibraryDatabase();
        MessageUtil.doDelayed(10, () -> RemoteLibraryDatabase.requestIndex());
    }

    /** Re-populates the {@link #allEntries} list from the available databases. Call this after changing one of them. */
    public static void fillEntries() {
        allEntries.clear();
        allEntries.addAll(BCLibDatabase.LOCAL_DB.getAllHeaders());
        if (BCLibDatabase.remoteDB != null) {
            for (LibraryEntryHeader header : BCLibDatabase.remoteDB.getAllHeaders()) {
                if (!allEntries.contains(header)) {
                    allEntries.add(header);
                }
            }
        }
        allEntries.sort(Comparator.naturalOrder());
    }

    public static EntryStatus getStatus(LibraryEntryHeader header) {
        boolean local = LOCAL_DB.getAllHeaders().contains(header);
        boolean remote = true;
        if (remoteDB != null) {
            remote = remoteDB.getAllHeaders().contains(header);
        }
        if (local) {
            if (remote) return EntryStatus.BOTH;
            else return EntryStatus.LOCAL;
        } else {
            if (remote) return EntryStatus.REMOTE;
            else return EntryStatus.NOWHERE;
        }
    }

    public enum EntryStatus {
        NOWHERE,
        REMOTE,
        LOCAL,
        BOTH;
    }
}
