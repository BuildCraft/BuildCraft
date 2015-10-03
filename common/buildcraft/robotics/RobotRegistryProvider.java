package buildcraft.robotics;

import java.util.HashMap;

import net.minecraft.world.World;

import net.minecraftforge.common.MinecraftForge;

import buildcraft.api.robots.DockingStation;
import buildcraft.api.robots.IRobotRegistryProvider;

public class RobotRegistryProvider implements IRobotRegistryProvider {
	private static HashMap<Integer, RobotRegistry> registries = new HashMap<Integer, RobotRegistry>();

	@Override
	public synchronized RobotRegistry getRegistry(World world) {
		if (!registries.containsKey(world.provider.dimensionId)
				|| registries.get(world.provider.dimensionId).world != world) {

			RobotRegistry newRegistry = (RobotRegistry) world.perWorldStorage.loadData(RobotRegistry.class, "robotRegistry");

			if (newRegistry == null) {
				newRegistry = new RobotRegistry("robotRegistry");
				world.perWorldStorage.setData("robotRegistry", newRegistry);
			}

			newRegistry.world = world;

			for (DockingStation d : newRegistry.stations.values()) {
				d.world = world;
			}

			MinecraftForge.EVENT_BUS.register(newRegistry);

			registries.put(world.provider.dimensionId, newRegistry);

			return newRegistry;
		}

		return registries.get(world.provider.dimensionId);
	}
}
