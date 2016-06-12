package buildcraft.lib.library;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.nbt.NBTTagCompound;

import buildcraft.api.core.BCDebugging;
import buildcraft.api.core.BCLog;
import buildcraft.lib.misc.data.ZipFileHelper;

public abstract class LibraryDatabase_Neptune {
    public static final boolean DEBUG = BCDebugging.shouldDebugLog("lib.library");
    public static final String HEADER = "header.nbt";

    protected final Map<LibraryEntryHeader, LibraryEntryData> entries = new HashMap<>();

    protected boolean addEntry(ZipFileHelper helper, String from, String kind) {
        if (helper == null) return false;
        if (helper.getKeys().isEmpty()) return false;
        try {
            addInternal(helper, kind);
        } catch (IOException io) {
            BCLog.logger.warn("[lib.library] Failed to add " + from + " because " + io.getMessage());
            if (DEBUG) {
                io.printStackTrace();
            }
            return false;
        }
        return true;
    }

    private void addInternal(ZipFileHelper helper, String kind) throws IOException {
        // Try and find the header
        NBTTagCompound headerData = helper.getNbtEntry(HEADER);
        LibraryEntryHeader header = new LibraryEntryHeader(headerData, kind);

    }

    public abstract void readAll();

    public void add(LibraryEntryHeader header, LibraryEntryData data) {
        entries.put(header, data);
        save(header, data);
    }

    protected abstract void save(LibraryEntryHeader header, LibraryEntryData data);
}
