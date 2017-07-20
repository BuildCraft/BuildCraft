/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.guide.data;

import buildcraft.api.core.BCDebugging;
import buildcraft.api.core.BCLog;
import buildcraft.lib.client.guide.loader.FolderLoadable;
import buildcraft.lib.client.guide.loader.ILoadableResource;
import buildcraft.lib.client.guide.loader.ZipLoadable;
import com.google.gson.Gson;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class GuideEntryLoader {
    public static final boolean DEBUG = BCDebugging.shouldDebugLog("lib.guide.loader");

    public static Map<JsonEntry, ILoadableResource> loadAll() {
        Map<JsonEntry, ILoadableResource> allEntries = new HashMap<>();

        Map<File, ILoadableResource> files = new HashMap<>();

        for (ModContainer mod : Loader.instance().getActiveModList()) {
            File source = mod.getSource();
            if (DEBUG) {
                BCLog.logger.info("[lib.guide.loader] Checking mod " + mod.getName() + ", from " + source);
            }

            if (source == null) {
                BCLog.logger.warn("[lib.guide.loader] Failed to load data for " + mod.getName() + " as it did not have a source!");
                continue;
            }

            if (!files.containsKey(source)) {
                if (source.isDirectory()) {
                    files.put(source, new FolderLoadable(source));
                } else if (source.isFile()) {
                    try {
                        files.put(source, new ZipLoadable(source));
                    } catch (IOException io) {
                        BCLog.logger.warn("[lib.guide.loader] Failed to load the jar file!", io);
                        continue;
                    }
                } else if (DEBUG) {
                    BCLog.logger.warn("[lib.guide.loader]   ... Apparently " + source + " did not exist! how?");
                    continue;
                } else {
                    BCLog.logger.warn("[lib.guide.loader] Failed to load data for " + mod.getName() + " as it did not exist! (" + source + ")");
                    continue;
                }
            }

            ILoadableResource loadable = files.get(source);

            JsonContents contents = loadContents(loadable, mod);

            if (contents != null) {
                if (DEBUG) {
                    contents.printContents();
                }
                contents = contents.inheritMissingTags();
                if (DEBUG) {
                    contents.printContents();
                }
                for (JsonEntry entry : contents.contents) {
                    allEntries.put(entry, loadable);
                }
            }
        }
        return allEntries;
    }

    private static JsonContents loadContents(ILoadableResource loadable, ModContainer mod) {
        String resource = "assets/" + mod.getModId() + "/guide/contents.json";

        try (InputStream is = loadable.getInputStreamFor(resource)) {
            if (is != null) {
                InputStreamReader isr = new InputStreamReader(is);
                return new Gson().fromJson(isr, JsonContents.class);
            }
            return null;
        } catch (IOException io) {
            if (DEBUG) {
                BCLog.logger.warn("[lib.guide.loader] Failed to load the json file!", io);
            } else {
                BCLog.logger.warn("[lib.guide.loader] Failed to load the json file from " + mod.getModId() + io.getMessage());
            }
            return null;
        }
    }
}
