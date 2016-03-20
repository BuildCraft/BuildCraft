package buildcraft.core.lib.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import buildcraft.api.core.BCLog;

public class FileConfigManager extends StreamConfigManager {
    private final String comment;
    private File file;

    public FileConfigManager(String comment) {
        this.comment = comment;
    }

    public void setConfigFile(File file) {
        this.file = file;
        read();
    }

    @Override
    protected void read() {
        if (file == null) {
            exportDefault();
            return;
        }
        if (!file.exists()) exportDefault();
        if (!file.exists()) write();
        try (FileInputStream in = new FileInputStream(file)) {
            read(in);
        } catch (FileNotFoundException e) {
            BCLog.logger.warn("Did not find the file! odd... while opening the detailed config file (" + file.getAbsolutePath() + ")");
        } catch (IOException e) {
            BCLog.logger.warn("Caught an IOException while reading the detailed config file: " + e.getMessage());
        }
    }

    public void exportDefault() {}

    @Override
    protected void write() {
        if (file == null) return;
        if (file.exists()) file.delete();
        try (FileOutputStream out = new FileOutputStream(file)) {
            super.write(out);
        } catch (FileNotFoundException e) {
            BCLog.logger.warn("Did not find the file! odd... while writing the detailed config file (" + file.getAbsolutePath() + ")");
        } catch (IOException e) {
            BCLog.logger.warn("Caught an IOException while writing the detailed config file: " + e.getMessage());
        }
    }

    @Override
    protected String comment() {
        return comment;
    }
}
