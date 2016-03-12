package buildcraft.core.lib.config;

import java.io.*;
import java.nio.file.Files;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Properties;
import java.util.TreeSet;

import org.apache.commons.io.IOUtils;

import buildcraft.api.core.BCLog;

/** A detailed config manager. These are for misc config options that change client side stuffs that don't make sense
 * elsewhere. */
public enum DetailedConfigManager {
    INSTANCE;
    // TODO: Put these into a wiki

    @SuppressWarnings("serial")
    private final Properties properties = new Properties() {
        @Override
        public synchronized Enumeration<Object> keys() {
            // A way of sorting the properties.
            return Collections.enumeration(new TreeSet<>(super.keySet()));
        }
    };
    private File file;

    public void setConfigFile(File file) {
        this.file = file;
        readFile();
    }

    /** @param option The option to refresh
     * @return True if something changed as a result of the refresh */
    boolean refresh(DetailedConfigOption option) {
        if (properties.getProperty(option.id) == null) readFile();
        if (properties.getProperty(option.id) == null) {
            properties.setProperty(option.id, option.defaultVal);
            option.cache = option.defaultVal;
            readAndWriteFile();
            return true;
        }
        String val = properties.getProperty(option.id, option.defaultVal);
        if (val.equals(option.cache)) return false;
        option.cache = val;
        readAndWriteFile();
        return true;
    }

    private void readAndWriteFile() {
        // Just refresh whatever was in the file
        readFile();
        writeFile();
    }

    private void readFile() {
        if (file == null) return;
        if (file.exists()) {
            try (FileReader reader = new FileReader(file)) {
                properties.load(reader);
                reader.close();
            } catch (FileNotFoundException e) {
                BCLog.logger.warn("Did not find the file! odd... while opening the detailed config file");
            } catch (IOException e) {
                BCLog.logger.warn("Caught an IOException while reading the detailed config file: " + e.getMessage());
            }
        } else {
            // Export the default file contained in this JAR file
            InputStream stream = DetailedConfigManager.class.getResourceAsStream("detailed.properties.default");
            if (stream != null) {
                try {
                    Files.copy(stream, file.toPath());
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    IOUtils.closeQuietly(stream);
                }
            }
        }
    }

    private void writeFile() {
        if (file == null) return;
        try (FileWriter writer = new FileWriter(file)) {
            String comment = " The buildcraft detailed configuration file. This contains a lot of miscelaneous options that have no "
                + "affect on gameplay.\n You should refer to the BC source code for a detailed description of what these do. "
                + "(https://github.com/BuildCraft/BuildCraft)\n"
                + " This file will be overwritten every time that buildcraft starts, so there is no point in adding comments";
            properties.store(writer, comment);
            writer.close();
        } catch (IOException e) {
            BCLog.logger.warn("Caught an IOException while writing the detailed config file: " + e.getMessage());
        }
    }
}
