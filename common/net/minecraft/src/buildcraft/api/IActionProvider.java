/** 
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src.buildcraft.api;

import java.util.LinkedList;

import net.minecraft.src.Block;
import net.minecraft.src.TileEntity;

public interface IActionProvider {
	
	/**
	 * Returns the list of actions available to a gate next to the given 
	 * block.
	 */
	public abstract LinkedList<Action> getNeighborActions(Block block,
			TileEntity tile);
	
}
