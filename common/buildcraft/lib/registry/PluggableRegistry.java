/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

// TODO(R.Chen): Fabric migration DEFERRED — blocked by unmigrated api.transport.pluggable
//               (IPluggableRegistry, PluggableDefinition → Direction/PacketByteBuf/IPipeHolder chain).
//               Forge-free otherwise; only needs Identifier → Identifier once transport lands.
package buildcraft.lib.registry;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.util.Identifier;

import buildcraft.api.transport.pluggable.IPluggableRegistry;
import buildcraft.api.transport.pluggable.PluggableDefinition;

public enum PluggableRegistry implements IPluggableRegistry {
    INSTANCE;

    private final Map<Identifier, PluggableDefinition> registered = new HashMap<>();

    @Override
    public void register(Identifier id, PluggableDefinition definition) {
        registered.put(id, definition);
    }

    @Override
    public PluggableDefinition getDefinition(Identifier identifier) {
        return registered.get(identifier);
    }
}
