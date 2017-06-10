/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.resource;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.util.ResourceLocation;

import buildcraft.api.core.BCLog;

public enum ResourceRegistry implements IResourceManagerReloadListener {
    INSTANCE;

    private final Map<ResourceLocation, ResourceHolder> holders = new HashMap<>();

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager) {
        for (ResourceHolder holder : holders.values()) {
            BCLog.logger.info("[lib.resource] Reloading " + holder.locationBase + " as " + holder.getClass());
            holder.reload(resourceManager);
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends ResourceHolder> T register(T holder, Class<T> wanted) {
        ResourceLocation loc = holder.locationBase;
        if (!holders.containsKey(loc)) {
            holders.put(loc, holder);
            return holder;
        }
        ResourceHolder existing = holders.get(loc);
        if (wanted.isInstance(existing)) {
            return (T) existing;
        } else {
            BCLog.logger.warn("[lib.resource] " + loc + " has an existing instance for " + existing.getClass() + ", being replaced by " + holder);
            holders.put(loc, holder);
            return holder;
        }
    }
}
