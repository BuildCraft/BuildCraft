package ct.buildcraft.silicon;

import ct.buildcraft.api.BCModules;
import ct.buildcraft.api.transport.pipe.PipeApi;
import ct.buildcraft.api.transport.pluggable.PluggableDefinition;
import ct.buildcraft.api.transport.pluggable.PluggableDefinition.IPluggableCreator;
import ct.buildcraft.api.transport.pluggable.PluggableDefinition.IPluggableNbtReader;
import ct.buildcraft.api.transport.pluggable.PluggableDefinition.IPluggableNetLoader;
import ct.buildcraft.silicon.plug.PluggableFacade;
import ct.buildcraft.silicon.plug.PluggableGate;
import ct.buildcraft.silicon.plug.PluggableLens;
import ct.buildcraft.silicon.plug.PluggableLightSensor;
import ct.buildcraft.silicon.plug.PluggablePulsar;
import net.minecraft.resources.ResourceLocation;

public class BCSiliconPlugs {
	
    public static PluggableDefinition gate;
    public static PluggableDefinition lens;
    public static PluggableDefinition pulsar;
    public static PluggableDefinition lightSensor;
    public static PluggableDefinition facade;

    public static void preInit() {
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

        // TODO: remove this in 7.99.19!
        // This handles the migration of most of the transport pluggables into silicon
        String modId = BCModules.TRANSPORT.getModId();
        PipeApi.pluggableRegistry.register(new ResourceLocation(modId, def.identifier.getPath()), def);
        return def;
    }

    private static ResourceLocation idFor(String name) {
        return new ResourceLocation("buildcraftsilicon", name);
    }

}
