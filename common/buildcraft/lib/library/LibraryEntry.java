package buildcraft.lib.library;

public final class LibraryEntry {
    public final LibraryEntryHeader header;
    public final ILibraryEntryData data;

    public LibraryEntry(LibraryEntryHeader header, ILibraryEntryData data) {
        this.header = header;
        this.data = data;
    }
}
