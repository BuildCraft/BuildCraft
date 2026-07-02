package ct.buildcraft.robotics;

import ct.buildcraft.api.transport.pipe.PipeApi;
import ct.buildcraft.api.transport.pluggable.PluggableDefinition;
import ct.buildcraft.robotics.plug.RobotStationPluggable;
import net.minecraft.resources.ResourceLocation;

public final class BCRoboticsPlugs {
    public static PluggableDefinition robotStation;

    private BCRoboticsPlugs() {
    }

    public static void preInit() {
        if (robotStation == null) {
            robotStation = new PluggableDefinition(idFor("robot_station"), RobotStationPluggable::readFromNbt, RobotStationPluggable::loadFromBuffer);
        }
        if (PipeApi.pluggableRegistry != null) {
            PipeApi.pluggableRegistry.register(robotStation);
        }
    }

    private static ResourceLocation idFor(String name) {
        return new ResourceLocation(BCRobotics.MODID, name);
    }
}
