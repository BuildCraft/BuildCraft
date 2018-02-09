/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport;

import net.minecraft.util.ResourceLocation;

import buildcraft.api.transport.pipe.PipeApi;
import buildcraft.api.transport.pluggable.PluggableDefinition;
import buildcraft.api.transport.pluggable.PluggableDefinition.IPluggableCreator;
import buildcraft.api.transport.pluggable.PluggableDefinition.IPluggableNbtReader;
import buildcraft.api.transport.pluggable.PluggableDefinition.IPluggableNetLoader;

import buildcraft.transport.plug.PluggableBlocker;
import buildcraft.transport.plug.PluggableFacade;
import buildcraft.transport.plug.PluggableGate;
import buildcraft.transport.plug.PluggableLens;
import buildcraft.transport.plug.PluggableLightSensor;
import buildcraft.transport.plug.PluggablePowerAdaptor;
import buildcraft.transport.plug.PluggablePulsar;

public class BCTransportPlugs {

    public static PluggableDefinition blocker;
    public static PluggableDefinition powerAdaptor;
    public static PluggableDefinition gate;
    public static PluggableDefinition lens;
    public static PluggableDefinition pulsar;
    public static PluggableDefinition lightSensor;
    public static PluggableDefinition facade;

    public static void preInit() {
        blocker = register("blocker", PluggableBlocker::new);
        powerAdaptor = register("power_adaptor", PluggablePowerAdaptor::new);
        gate = register("gate", PluggableGate::new, PluggableGate::new);
        lens = register("lens", PluggableLens::new, PluggableLens::new);
        pulsar = register("pulsar", PluggablePulsar::new, PluggablePulsar::new);
        lightSensor = register("daylight_sensor", PluggableLightSensor::new);
        facade = register("facade", PluggableFacade::new, PluggableFacade::new);
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
