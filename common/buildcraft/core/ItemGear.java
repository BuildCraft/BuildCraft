/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core;

import buildcraft.core.science.IItemTechnologyProvider;

public class ItemGear extends ItemBuildCraft implements IItemTechnologyProvider {

	private int processTime;

	public ItemGear(int iProcessTime) {
		processTime = iProcessTime;
	}

	@Override
	public int timeToProcess() {
		return processTime;
	}

}
