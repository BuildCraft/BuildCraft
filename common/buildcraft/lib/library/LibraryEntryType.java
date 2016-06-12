package buildcraft.lib.library;

import java.io.IOException;

import buildcraft.lib.misc.data.ZipFileHelper;

public interface LibraryEntryType {
    LibraryEntryData read(ZipFileHelper file) throws IOException;
}
