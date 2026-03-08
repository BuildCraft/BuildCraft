package buildcraft.robotics;

import buildcraft.api.robots.DockingStation;
import buildcraft.api.robots.IRobotRegistryProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;

import java.util.HashMap;

public class RobotRegistryProvider implements IRobotRegistryProvider {
    // private static HashMap<Integer, IRobotRegistry> registries = new HashMap<Integer, IRobotRegistry>();
    private static HashMap<ResourceKey<Level>, RobotRegistry> registries = new HashMap<ResourceKey<Level>, RobotRegistry>();

    @Override
    public synchronized RobotRegistry getRegistry(Level world) {
        // if (!registries.containsKey(world.provider.getDimensionId()) || registries.get(world.provider.getDimensionId()).world != world)
        if (!registries.containsKey(world.dimension()) || registries.get(world.dimension()).world != world) {

            // RobotRegistry newRegistry = (RobotRegistry) world.getPerWorldStorage().loadData(RobotRegistry.class, "robotRegistry");
            RobotRegistry newRegistry = (RobotRegistry) ((ServerLevel) world).getDataStorage().get((nbt) -> {
                RobotRegistry ret = new RobotRegistry();
                ret.readFromNBT(nbt);
                return ret;
            }, "robotRegistry");

            if (newRegistry == null) {
                // newRegistry = new RobotRegistry("robotRegistry");
                newRegistry = new RobotRegistry();
                // world.getPerWorldStorage().setData("robotRegistry", newRegistry);
                ((ServerLevel) world).getDataStorage().set("robotRegistry", newRegistry);
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
