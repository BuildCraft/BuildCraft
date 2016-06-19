package buildcraft.lib.library;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.nbt.NBTTagCompound;

import buildcraft.api.core.BCDebugging;
import buildcraft.api.core.BCLog;
import buildcraft.api.data.NBTSquishConstants;
import buildcraft.lib.BCLibDatabase;
import buildcraft.lib.misc.data.ZipFileHelper;

public abstract class LibraryDatabase_Neptune {
    public static final boolean DEBUG = BCDebugging.shouldDebugLog("lib.library");
    public static final String HEADER = "header.nbt";

    protected final Map<LibraryEntryHeader, LibraryEntryData> entries = new HashMap<>();

    protected boolean addFromZip(ZipFileHelper helper, String from) {
        if (helper == null) return false;
        if (helper.getKeys().isEmpty()) return false;
        try {
            addInternal(helper);
        } catch (IOException io) {
            BCLog.logger.warn("[lib.library] Failed to add " + from + " because " + io.getMessage());
            if (DEBUG) {
                io.printStackTrace();
            }
            return false;
        }
        return true;
    }

    private void addInternal(ZipFileHelper helper) throws IOException {
        // Try and find the header
        NBTTagCompound headerData = helper.getNbtEntry(HEADER);
        LibraryEntryHeader header = new LibraryEntryHeader(headerData);
        String kind = header.kind;
        LibraryEntryType type = BCLibDatabase.REGISTERED_TYPES.get(kind);
        if (type == null) {
            throw new IOException("Unkown kind " + kind);
        }
        LibraryEntryData data = type.read(helper);
        entries.put(header, data);
    }

    /** Adds a new entry.
     * 
     * @return True if this changed the map at all. */
    public boolean addNew(LibraryEntryHeader header, LibraryEntryData data) {
        LibraryEntryData old = entries.put(header, data);
        return !Objects.equals(old, data);
    }

    public static void save(OutputStream out, LibraryEntryHeader header, LibraryEntryData data) {
        try (ZipOutputStream zos = new ZipOutputStream(out)) {
            ZipFileHelper helper = new ZipFileHelper();
            helper.addNbtEntry(HEADER, "", header.writeToNBT(), NBTSquishConstants.VANILLA);
            data.write(helper);
            helper.write(zos);
            zos.finish();
            zos.flush();
        } catch (IOException io) {
            io.printStackTrace();
        }
    }

    public static Entry<LibraryEntryHeader, LibraryEntryData> load(InputStream in) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(in)) {
            ZipFileHelper helper = new ZipFileHelper(zis);
            NBTTagCompound headerNbt = helper.getNbtEntry(HEADER);
            LibraryEntryHeader header = new LibraryEntryHeader(headerNbt);
            LibraryEntryType type = BCLibDatabase.REGISTERED_TYPES.get(header.kind);
            if (type == null) {
                throw new IOException("Unkown kind " + header.kind);
            }
            LibraryEntryData data = type.read(helper);
            return Pair.of(header, data);
        }
    }

    public abstract Collection<LibraryEntryHeader> getAllHeaders();
}
