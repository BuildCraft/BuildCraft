/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.science;

/**
 * This interface is implemented by items that provide specific research points.
 * In particular, it gives how many cycles are required to process the items
 */
public interface IItemTechnologyProvider {

	int timeToProcess();

}
