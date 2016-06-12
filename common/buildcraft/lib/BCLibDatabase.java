package buildcraft.lib;

import java.util.HashMap;
import java.util.Map;

import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientConnectedToServerEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.lib.library.LibraryEntryType;
import buildcraft.lib.library.LocalLibraryDatabase;
import buildcraft.lib.library.RemoteLibraryDatabase;
import buildcraft.lib.library.book.LibraryEntryBook;
import buildcraft.lib.misc.WorkerThreadUtil;

public class BCLibDatabase {
    public static final Map<String, LibraryEntryType> REGISTERED_TYPES = new HashMap<>();
    public static final LocalLibraryDatabase LOCAL_DB = new LocalLibraryDatabase();
    public static RemoteLibraryDatabase remoteDB = null;

    static {
        REGISTERED_TYPES.put(LibraryEntryBook.KIND, LibraryEntryBook::new);
    }

    public static void fmlInit() {
        WorkerThreadUtil.executeDependantTask(LOCAL_DB::readAll);
    }

    @SideOnly(Side.CLIENT)
    public static void connectToServer(ClientConnectedToServerEvent event) {
        if (event.isLocal()) {
            remoteDB = null;
            return;
        }
        remoteDB = new RemoteLibraryDatabase();
    }
}
