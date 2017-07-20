/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.resource;

import buildcraft.api.core.BCLog;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.io.IOUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public abstract class ResourceHolder {
    public final ResourceLocation locationBase;

    public ResourceHolder(ResourceLocation location) {
        this.locationBase = location;
    }

    public static ResourceLocation getForLang(ResourceLocation location, boolean useFallback) {
        String domain = location.getResourceDomain();
        String path = location.getResourcePath();
        final String lang;
        if (useFallback) {
            lang = "en_US";
        } else {
            lang = Minecraft.getMinecraft().getLanguageManager().getCurrentLanguage().getLanguageCode();
        }
        path = path.replaceFirst("guide", "guide/" + lang);
        return new ResourceLocation(domain, path);
    }

    final void reload(IResourceManager resourceManager) {
        onLoad(load(resourceManager));
    }

    public ResourceLocation getLocationForLang(boolean useFallback) {
        return getForLang(locationBase, useFallback);
    }

    protected abstract void onLoad(byte[] data);

    protected byte[] load(IResourceManager resourceManager) {
        byte[] loaded = load(resourceManager, getLocationForLang(false), false);
        if (loaded != null) return loaded;
        loaded = load(resourceManager, getLocationForLang(true), true);
        if (loaded != null) return loaded;

        return new byte[0];
    }

    private static byte[] load(IResourceManager resourceManager, ResourceLocation location, boolean care) {
        try (IResource res = resourceManager.getResource(location)) {
            try (InputStream input = res.getInputStream()) {
                return IOUtils.toByteArray(input);
            }
        } catch (FileNotFoundException e) {
            if (care) {
                BCLog.logger.warn("[lib.resource] The file " + location + " was not found! (" + e.getMessage() + ")");
            }
        } catch (IOException e) {
            // Some error, we DO care about it no matter what.
            e.printStackTrace();
        }
        return null;
    }
}
