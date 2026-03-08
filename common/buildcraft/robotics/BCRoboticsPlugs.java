package buildcraft.robotics;

import buildcraft.api.transport.pipe.PipeApi;
import buildcraft.api.transport.pluggable.PluggableDefinition;
import buildcraft.robotics.plug.PluggableRobotStation;
import net.minecraft.resources.ResourceLocation;

public class BCRoboticsPlugs {
    public static PluggableDefinition robotStation;

    public static void preInit() {
        robotStation = register("robot_station", PluggableRobotStation::new, PluggableRobotStation::new);
    }

    private static PluggableDefinition register(String name, PluggableDefinition.IPluggableNbtReader reader, PluggableDefinition.IPluggableNetLoader loader) {
        return register(new PluggableDefinition(idFor(name), reader, loader));
    }

    private static PluggableDefinition register(PluggableDefinition def) {
        // TODO: Add config for enabling/disabling
        PipeApi.pluggableRegistry.register(def);

//        // TODO: remove this in 7.99.19!
//        // This handles the migration of most of the transport pluggables into silicon
//        String modId = BCModules.TRANSPORT.getModId();
//        PipeApi.pluggableRegistry.register(new ResourceLocation(modId, def.identifier.getPath()), def);
        return def;
    }

    private static ResourceLocation idFor(String name) {
        return new ResourceLocation("buildcraftrobotics", name);
    }
}
