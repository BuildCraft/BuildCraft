/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.config;

import buildcraft.api.core.BCLog;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Properties;
import java.util.TreeSet;

@Deprecated
public abstract class StreamConfigManager {
    @SuppressWarnings("serial")
    private final Properties properties = new Properties() {
        @Override
        public synchronized Enumeration<Object> keys() {
            // A way of sorting the properties.
            return Collections.enumeration(new TreeSet<>(super.keySet()));
        }
    };

    /** @param option The option to refresh
     * @return True if something changed as a result of the refresh. */
    protected final boolean refresh(DetailedConfigOption option, String key) {
        if (properties.getProperty(key) == null) read();
        if (properties.getProperty(key) == null) {
            properties.setProperty(key, option.defaultValue());
            option.cache = option.defaultValue();
            readAndWriteFile();
            return true;
        }
        String val = properties.getProperty(key, option.defaultValue());
        if (val.equals(option.cache)) return false;
        option.cache = val;
        readAndWriteFile();
        return true;
    }

    protected void readAndWriteFile() {
        // Just refresh whatever was in it
        read();
        write();
    }

    protected abstract void read();

    protected final void read(InputStream streamIn) {
        if (streamIn != null) {
            try (Reader reader = new InputStreamReader(streamIn, StandardCharsets.UTF_8)) {
                properties.load(reader);
                reader.close();
            } catch (IOException e) {
                BCLog.logger.warn("Caught an IOException while reading the detailed config file: " + e.getMessage());
            }
        }
    }

    protected abstract void write();

    protected final void write(OutputStream streamOut) {
        if (streamOut == null) return;
        try (Writer writer = new OutputStreamWriter(streamOut, StandardCharsets.UTF_8)) {
            properties.store(writer, comment());
            writer.close();
        } catch (IOException e) {
            BCLog.logger.warn("Caught an IOException while writing the detailed config file: " + e.getMessage());
        }
    }

    protected abstract String comment();
}
