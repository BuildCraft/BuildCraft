package buildcraft.robotics;

import buildcraft.api.robots.DockingStation;
import buildcraft.api.robots.IRobotRegistryProvider;
import net.minecraft.util.RegistryKey;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;

import java.util.HashMap;

public class RobotRegistryProvider implements IRobotRegistryProvider {
    // private static HashMap<Integer, IRobotRegistry> registries = new HashMap<Integer, IRobotRegistry>();
    private static HashMap<RegistryKey<World>, RobotRegistry> registries = new HashMap<RegistryKey<World>, RobotRegistry>();

    @Override
    public synchronized RobotRegistry getRegistry(World world) {
        // if (!registries.containsKey(world.provider.getDimensionId()) || registries.get(world.provider.getDimensionId()).world != world)
        if (!registries.containsKey(world.dimension()) || registries.get(world.dimension()).world != world) {

            // RobotRegistry newRegistry = (RobotRegistry) world.getPerWorldStorage().loadData(RobotRegistry.class, "robotRegistry");
            RobotRegistry newRegistry = (RobotRegistry) ((ServerWorld) world).getDataStorage().get(
                    () -> new RobotRegistry("robotRegistry")
                    , "robotRegistry");

            if (newRegistry == null) {
                newRegistry = new RobotRegistry("robotRegistry");
                // world.getPerWorldStorage().setData("robotRegistry", newRegistry);
                ((ServerWorld) world).getDataStorage().set(newRegistry);
            }

            newRegistry.world = world;

            for (DockingStation d : newRegistry.stations.values()) {
                d.world = world;
            }

            MinecraftForge.EVENT_BUS.register(newRegistry);

            registries.put(world.dimension(), newRegistry);

            return newRegistry;
        }

        return registries.get(world.dimension());
    }
}
