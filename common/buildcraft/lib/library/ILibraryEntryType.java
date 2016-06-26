package buildcraft.lib.library;

import java.io.IOException;

import buildcraft.lib.misc.data.ZipFileHelper;

public interface ILibraryEntryType {
    ILibraryEntryData read(ZipFileHelper file) throws IOException;
}
