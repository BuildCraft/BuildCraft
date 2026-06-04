/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */
package buildcraft.silicon;

import net.minecraft.util.Identifier;

import buildcraft.api.transport.pipe.PipeApi;
import buildcraft.api.transport.pluggable.PluggableDefinition;
import buildcraft.api.transport.pluggable.PluggableDefinition.IPluggableCreator;
import buildcraft.api.transport.pluggable.PluggableDefinition.IPluggableNbtReader;
import buildcraft.api.transport.pluggable.PluggableDefinition.IPluggableNetLoader;

import buildcraft.silicon.plug.PluggableGate;
import buildcraft.silicon.plug.PluggablePulsar;

public class BCSiliconPlugs {

    public static PluggableDefinition gate;
    public static PluggableDefinition lens;
    public static PluggableDefinition pulsar;
    public static PluggableDefinition lightSensor;
    public static PluggableDefinition timer;
    public static PluggableDefinition facade;

    public static void preInit() {
        gate = register("gate", PluggableGate::new, PluggableGate::new);
        // STUB(R.Chen): PluggableLens not in libLeaf — deferred until lens is migrated.
        // lens = register("lens", PluggableLens::new, PluggableLens::new);
        pulsar = register("pulsar", PluggablePulsar::new, PluggablePulsar::new);
        // STUB(R.Chen): PluggableLightSensor not in libLeaf — deferred.
        // lightSensor = register("daylight_sensor", PluggableLightSensor::new);
        // STUB(R.Chen): PluggableTimer not in libLeaf — deferred.
        // timer = register("timer", PluggableTimer::new);
        // STUB(R.Chen): PluggableFacade not in libLeaf — deferred.
        // facade = register("facade", PluggableFacade::new, PluggableFacade::new);
    }

    private static PluggableDefinition register(String name, IPluggableCreator creator) {
        return register(new PluggableDefinition(idFor(name), creator));
    }

    private static PluggableDefinition register(String name, IPluggableNbtReader reader, IPluggableNetLoader loader) {
        return register(new PluggableDefinition(idFor(name), reader, loader));
    }

    private static PluggableDefinition register(PluggableDefinition def) {
        // TODO(R.Chen): Add config for enabling/disabling
        PipeApi.pluggableRegistry.register(def);

        // TODO(R.Chen): remove in 7.99.19!
        // This handles the migration of most of the transport pluggables into silicon.
        // In Forge the legacy registry used BCModules.TRANSPORT.getModId();
        // in Fabric the transport mod id is "buildcrafttransport".
        String modId = "buildcrafttransport";
        PipeApi.pluggableRegistry.register(new Identifier(modId, def.identifier.getPath()), def);
        return def;
    }

    private static Identifier idFor(String name) {
        return new Identifier("buildcraftsilicon", name);
    }
}
