package buildcraft.lib.library;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import net.minecraft.launchwrapper.Launch;

import buildcraft.api.core.BCLog;
import buildcraft.api.data.NBTSquishConstants;
import buildcraft.lib.BCLibDatabase;
import buildcraft.lib.misc.WorkerThreadUtil;
import buildcraft.lib.misc.data.ZipFileHelper;

public class LocalLibraryDatabase extends LibraryDatabase_Neptune {
    public File outDirectory;
    public final List<File> inDirectories = new ArrayList<>();

    public LocalLibraryDatabase() {
        final File dir;

        if (Launch.minecraftHome != null) {
            dir = new File(Launch.minecraftHome, "blueprints");// TODO: Config!
        } else {
            try {
                dir = new File("./blueprints").getCanonicalFile();
            } catch (IOException e) {
                throw new RuntimeException("Unable to get the current run directory!", e);
            }
        }
        outDirectory = dir;
        inDirectories.add(dir);
    }

    @Override
    public void readAll() {
        List<String> endings = new ArrayList<>();
        for (String key : BCLibDatabase.REGISTERED_TYPES.keySet()) {
            endings.add("." + key);
        }
        for (File in : inDirectories) {
            BCLog.logger.info("Reading from dir " + in);
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
        BCLog.logger.info("Found a possible file " + file);
        String name = file.getName();
        String[] split = name.split("\\.");
        String last = split[split.length - 1];
        LibraryEntryType type = BCLibDatabase.REGISTERED_TYPES.get(last);
        if (type == null) {
            return;
        }
        try (FileInputStream fis = new FileInputStream(file)) {
            ZipInputStream zis = new ZipInputStream(fis);
            ZipFileHelper helper = new ZipFileHelper(zis);
            addEntry(helper, file.getAbsolutePath(), last);
        } catch (IOException io) {
            io.printStackTrace();
        }
    }

    @Override
    protected void save(LibraryEntryHeader header, LibraryEntryData data) {
        String name = header.name.replace('/', '-').replace("\\", "-") + " - ";
        name += header.creation.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        File toSaveTo = new File(this.outDirectory, name + "." + header.kind);
        int toTry = 1;
        while (toSaveTo.isFile()) {
            toSaveTo = new File(outDirectory, name + " (" + toTry + ")." + header.kind);
            toTry++;
        }
        // Its a new file
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(toSaveTo))) {
            ZipFileHelper helper = new ZipFileHelper(HEADER);
            helper.addNbtEntry(HEADER, "", header.writeToNBT(), NBTSquishConstants.VANILLA);
            data.write(helper);
            helper.write(zos);
        } catch (IOException io) {
            io.printStackTrace();
        }
    }
}
