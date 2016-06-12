package buildcraft.lib.library;

import buildcraft.lib.misc.data.ZipFileHelper;

public interface LibraryEntryData {
    LibraryEntryHeader write(ZipFileHelper file);
}
