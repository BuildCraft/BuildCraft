package ct.buildcraft.robotics;

import ct.buildcraft.api.transport.pipe.PipeApiClient;
import ct.buildcraft.api.transport.pluggable.IPluggableStaticBaker;
import ct.buildcraft.lib.client.model.ModelHolderStatic;
import ct.buildcraft.lib.client.model.plug.PlugBakerSimple;
import ct.buildcraft.robotics.client.model.key.KeyRobotStation;
import net.minecraftforge.client.event.ModelEvent.BakingCompleted;

public final class BCRoboticsModels {
    public static final ModelHolderStatic ROBOT_STATION = new ModelHolderStatic("buildcraftrobotics:pluggables/robot_station");
    public static final IPluggableStaticBaker<KeyRobotStation> BAKER_ROBOT_STATION = new PlugBakerSimple<>(ROBOT_STATION::getCutoutQuads);

    private BCRoboticsModels() {
    }

    public static void init() {
        PipeApiClient.registry.registerBaker(KeyRobotStation.class, BAKER_ROBOT_STATION);
    }

    public static void onModelBake(BakingCompleted event) {
        PipeApiClient.registry.registerBaker(KeyRobotStation.class, BAKER_ROBOT_STATION);
    }
}
