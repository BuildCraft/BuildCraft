package buildcraft.lib;

import java.util.HashMap;
import java.util.Map;

import buildcraft.lib.library.LibraryDatabase_Neptune;
import buildcraft.lib.library.LibraryEntryType;
import buildcraft.lib.library.LocalLibraryDatabase;
import buildcraft.lib.library.book.LibraryEntryBook;
import buildcraft.lib.misc.WorkerThreadUtil;

public class BCLibDatabase {
    public static final Map<String, LibraryEntryType> REGISTERED_TYPES = new HashMap<>();
    public static final LocalLibraryDatabase LOCAL_DB = new LocalLibraryDatabase();
    public static final LibraryDatabase_Neptune REMOTE_DB = null;

    static {
        REGISTERED_TYPES.put(LibraryEntryBook.KIND, LibraryEntryBook::new);
    }

    public static void fmlInit() {
        WorkerThreadUtil.executeDependantTask(LOCAL_DB::readAll);
    }

}
