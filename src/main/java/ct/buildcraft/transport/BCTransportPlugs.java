/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.transport;

import net.minecraft.resources.ResourceLocation;

import ct.buildcraft.api.transport.pipe.PipeApi;
import ct.buildcraft.api.transport.pluggable.PluggableDefinition;
import ct.buildcraft.api.transport.pluggable.PluggableDefinition.IPluggableCreator;
import ct.buildcraft.api.transport.pluggable.PluggableDefinition.IPluggableNbtReader;
import ct.buildcraft.api.transport.pluggable.PluggableDefinition.IPluggableNetLoader;

import ct.buildcraft.transport.plug.PluggableBlocker;
import ct.buildcraft.transport.plug.PluggablePowerAdaptor;

public class BCTransportPlugs {

    public static PluggableDefinition blocker;
    public static PluggableDefinition powerAdaptor;

    public static void preInit() {
        blocker = register("blocker", PluggableBlocker::new);
        powerAdaptor = register("power_adaptor", PluggablePowerAdaptor::new);
    }

    private static PluggableDefinition register(String name, IPluggableCreator creator) {
        return register(new PluggableDefinition(idFor(name), creator));
    }

    private static PluggableDefinition register(String name, IPluggableNbtReader reader, IPluggableNetLoader loader) {
        return register(new PluggableDefinition(idFor(name), reader, loader));
    }

    private static PluggableDefinition register(PluggableDefinition def) {
        // TODO: Add config for enabling/disabling
        PipeApi.pluggableRegistry.register(def);
        return def;
    }

    private static ResourceLocation idFor(String name) {
        return new ResourceLocation("buildcrafttransport", name);
    }
}
