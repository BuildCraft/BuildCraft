package buildcraft.lib.library;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Stream;

import net.minecraft.launchwrapper.Launch;

import buildcraft.api.core.BCLog;
import buildcraft.lib.BCLibDatabase;
import buildcraft.lib.library.book.LibraryEntryBook;
import buildcraft.lib.misc.WorkerThreadUtil;

/** A local database. Stores the current */
public class LocalLibraryDatabase extends LibraryDatabase_Neptune {
    private static final String[] OLD_DB_LOCATIONS = { "blueprints" };
    private static final String DB_LOCATION = "bc-database";

    public File outDirectory;
    public Map<String, File> specificOutDirectories = new HashMap<>();
    public final List<File> inDirectories = new ArrayList<>();

    public LocalLibraryDatabase() {
        final File dir;

        final String dbLocation = DB_LOCATION;

        if (Launch.minecraftHome != null) {
            dir = Launch.minecraftHome;// TODO: Config!
        } else {
            try {
                dir = new File(".").getCanonicalFile();
            } catch (IOException e) {
                throw new RuntimeException("[lib.library] Unable to get the current run directory!", e);
            }
        }
        outDirectory = new File(dir, dbLocation);
        inDirectories.add(new File(dir, dbLocation));
        for (String old : OLD_DB_LOCATIONS) {
            inDirectories.add(new File(dir, old));
        }

        specificOutDirectories.put(LibraryEntryBook.KIND, new File(outDirectory, "books"));
    }

    public void onServerStarted() {
        for (LibraryEntryHeader header : entries.keySet()) {
            /* If we started BEFORE the server was initialised then the owner lookup would have failed.
             * 
             * This makes sure that it is always right. */
            header.author.fillOwner();
        }
    }

    public void readAll() {
        List<String> endings = new ArrayList<>();
        for (String key : BCLibDatabase.FILE_HANDLERS.keySet()) {
            endings.add("." + key);
        }
        for (File in : inDirectories) {
            BCLog.logger.info("[lib.library] Reading from dir " + in);
            if (in.exists()) {
                if (in.isDirectory()) {
                    try (Stream<Path> fileStream = Files.walk(in.toPath())) {
                        fileStream.forEach(this::readPath);
                    } catch (IOException io) {
                        io.printStackTrace();
                    }
                }
            }
        }
    }

    private void readPath(Path path) {
        WorkerThreadUtil.executeWorkTask(() -> {
            readFile(path.toFile());
        });
    }

    private void readFile(File file) {
        if (!file.isFile()) {
            return;
        }
        BCLog.logger.info("[lib.library] Found a possible file " + file);
        String name = file.getName();
        String[] split = name.split("\\.");
        String last = split[split.length - 1];
        ILibraryEntryType type = BCLibDatabase.FILE_HANDLERS.get(last);
        if (type == null) {
            return;
        }
        try (FileInputStream fis = new FileInputStream(file)) {
            Entry<LibraryEntryHeader, ILibraryEntryData> loaded = load(fis);
            LibraryEntryHeader header = loaded.getKey();
            ILibraryEntryData data = loaded.getValue();
            entries.put(header, data);
            BCLog.logger.info("[lib.library] Added a library entry " + header);
        } catch (IOException io) {
            BCLog.logger.warn("[lib.library] Failed to add " + file + " because " + io.getMessage());
            if (DEBUG) {
                io.printStackTrace();
            }
        }
    }

    @Override
    public boolean addNew(LibraryEntryHeader header, ILibraryEntryData data) {
        if (super.addNew(header, data)) {
            save(header, data);
            return true;
        } else {
            return false;
        }
    }

    protected void save(LibraryEntryHeader header, ILibraryEntryData data) {
        String name = header.name.replace('/', '-').replace("\\", "-") + " - ";
        name += header.creation.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        File out = getOutDirectory(header.kind);
        out.mkdirs();
        File toSaveTo = new File(out, name + "." + header.kind);
        int toTry = 1;
        while (toSaveTo.isFile()) {
            toSaveTo = new File(out, name + " (" + toTry + ")." + header.kind);
            toTry++;
        }
        // Its a new file
        try (FileOutputStream fos = new FileOutputStream(toSaveTo)) {
            save(fos, header, data);
        } catch (IOException io) {
            io.printStackTrace();
        }
    }

    public File getOutDirectory(String kind) {
        File specific = specificOutDirectories.get(kind);
        if (specific != null) {
            return specific;
        }
        return outDirectory;
    }

    public ILibraryEntryData getEntry(LibraryEntryHeader header) {
        return entries.get(header);
    }

    @Override
    public Collection<LibraryEntryHeader> getAllHeaders() {
        return entries.keySet();
    }
}
