/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.guide.data;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;

import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

import buildcraft.api.core.BCDebugging;
import buildcraft.api.core.BCLog;

public class GuideEntryLoader {
    public static final boolean DEBUG = BCDebugging.shouldDebugLog("lib.guide.loader");

    public static List<JsonEntry> loadAll(IResourceManager resourceManager) {
        List<JsonEntry> allEntries = new ArrayList<>();

        for (String domain : resourceManager.getResourceDomains()) {

            JsonContents contents = loadContents(resourceManager, domain);

            if (contents != null) {
                contents = contents.inheritMissingTags();
                for (JsonEntry entry : contents.contents) {
                    allEntries.add(entry);
                }
            }
        }
        return allEntries;
    }

    private static JsonContents loadContents(IResourceManager resourceManager, String domain) {
        ResourceLocation location = new ResourceLocation(domain, "compat/buildcraft/guide/contents.json");

        try (InputStream is = resourceManager.getResource(location).getInputStream()) {
            if (is != null) {
                InputStreamReader isr = new InputStreamReader(is);
                return new Gson().fromJson(isr, JsonContents.class);
            }
            return null;
        } catch (FileNotFoundException fnfe) {
            if (DEBUG) {
                BCLog.logger.warn(
                    "[lib.guide.loader] Looks like there is no guide contents page for " + location + ", skipping.");
            }
            return null;
        } catch (IOException io) {
            if (DEBUG) {
                BCLog.logger.warn("[lib.guide.loader] Failed to load the contents file for " + domain + "!", io);
            } else {
                BCLog.logger
                    .warn("[lib.guide.loader] Failed to load the contents file for " + domain + ": " + io.getMessage());
            }
            return null;
        }
    }
}
