/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.config;

import buildcraft.lib.BCLibProxy;
import net.minecraft.resources.ResourceLocation;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@Deprecated(forRemoval = true)
public class RoamingConfigManager extends StreamConfigManager {
    private static final Map<ResourceLocation, RoamingConfigManager> instances = new HashMap<>();
    private final ResourceLocation identifier;
    private Boolean cacheExists = null;

    public static RoamingConfigManager getOrCreateDefault(ResourceLocation identifier) {
        if (!instances.containsKey(identifier)) {
            instances.put(identifier, new RoamingConfigManager(identifier));
        }
        return instances.get(identifier);
    }

    public RoamingConfigManager(ResourceLocation identifier) {
        this.identifier = identifier;
    }

    @Override
    protected void read() {
        cacheExists = null;
        try (InputStream stream = BCLibProxy.getProxy().getStreamForIdentifier(identifier)) {
            read(stream);
            cacheExists = Boolean.TRUE;
        } catch (FileNotFoundException e) {
            // We can safely ignore this
        } catch (IOException io) {
            throw new Error("Failed to read from " + identifier, io);
        }
        if (cacheExists == null) {
            cacheExists = Boolean.FALSE;
        }
    }

    public boolean exists() {
        if (cacheExists == null) read();
        return cacheExists;
    }

    @Override
    protected void write() {
    }

    @Override
    protected String comment() {
        return null;
    }
}
