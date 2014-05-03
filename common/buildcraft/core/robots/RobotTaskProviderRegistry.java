/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.robots;

import java.util.ArrayList;
import java.util.Collections;

public final class RobotTaskProviderRegistry {

	public static int callNb = 0;

	private static ArrayList<IRobotTaskProvider> providers = new ArrayList<IRobotTaskProvider>();

	/**
	 * Deactivate constructor
	 */
	private RobotTaskProviderRegistry() {
	}

	public static void registerProvider (IRobotTaskProvider provider) {
		providers.add(provider);
	}

	public static void scanForTask (EntityRobot robot) {
		callNb++;

		if (callNb >= 31 /*prime number, could be bigger */) {
			Collections.shuffle (providers);
		}

		for (int i = providers.size() - 1; i >= 0; --i) {
			if (!providers.get(i).isActive()) {
				providers.remove(i);
			} else {
				IRobotTaskProvider provider = providers.get(i);

				if (provider.getWorld() == robot.worldObj) {
					double dx = robot.posX - provider.getX();
					double dy = robot.posY - provider.getY();
					double dz = robot.posZ - provider.getZ();

					// TODO: 30 blocks is the current magic constant for robot
					// task scan. Could be variable instead.
					if (dx * dx + dy * dy + dz * dz < 30 * 30) {
						IRobotTask task = provider.getNextTask(robot);

						if (task != null && robot.acceptTask(task)) {
							robot.currentTask = task;
							task.setup(robot);
							provider.popNextTask();

							return;
						}
					}
				}
			}
		}
	}

}
