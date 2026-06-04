/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */
package buildcraft.transport;

import net.minecraft.util.Identifier;

import buildcraft.api.transport.pipe.PipeApi;
import buildcraft.api.transport.pluggable.PluggableDefinition;
import buildcraft.api.transport.pluggable.PluggableDefinition.IPluggableCreator;

import buildcraft.transport.plug.PluggableBlocker;
import buildcraft.transport.plug.PluggablePowerAdaptor;

public class BCTransportPlugs {

    public static PluggableDefinition blocker;
    public static PluggableDefinition powerAdaptor;

    public static void preInit() {
        blocker = register("blocker", PluggableBlocker::new);
        powerAdaptor = register("power_adaptor", PluggablePowerAdaptor::new);
    }

    private static PluggableDefinition register(String name, IPluggableCreator creator) {
        return register(new PluggableDefinition(new Identifier("buildcrafttransport", name), creator));
    }

    private static PluggableDefinition register(PluggableDefinition def) {
        PipeApi.pluggableRegistry.register(def);
        return def;
    }
}
