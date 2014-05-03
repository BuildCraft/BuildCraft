/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.robots;

import net.minecraft.world.World;

/**
 * Objects able to provide tasks to robots. They should be registered by
 * #RobotTaskProviderRegistry
 */
public interface IRobotTaskProvider {

	/**
	 * Robots will pick up tasks that are provided within a certain range.
	 * These can be coming from e.g. tile entities or entities.
	 */
	double getX();

	double getY();

	double getZ();

	World getWorld();

	/**
	 * If the provider is not active, it will be eventually removed from the
	 * list of potential providers.
	 */
	boolean isActive();

	/**
	 * Returns the next task that can be given to the robot in parameter. This
	 * is a first level of filter. The robot may or may not decide to pick up
	 * the task.
	 */
	IRobotTask getNextTask(EntityRobot robot);

	/**
	 * This is called once a task has been accepted, to be removed from the
	 * list of available tasks.
	 */
	void popNextTask();

}
