/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.registry;

import buildcraft.api.transport.pluggable.IPluggableRegistry;
import buildcraft.api.transport.pluggable.PluggableDefinition;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public enum PluggableRegistry implements IPluggableRegistry {
    INSTANCE;

    // private final Map<ResourceLocation, PluggableDefinition> registered = new HashMap<>();
    private final Map<ResourceLocation, PluggableDefinition> registered = new ConcurrentHashMap<>();

    @Override
    public void register(ResourceLocation id, PluggableDefinition definition) {
        registered.put(id, definition);
    }

    @Override
    public PluggableDefinition getDefinition(ResourceLocation identifier) {
        return registered.get(identifier);
    }
}
