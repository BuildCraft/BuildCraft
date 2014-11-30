/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.api.tiles;

/**
 * This interface should be implemented by any Tile Entity which carries out
 * work (crafting, ore processing, mining, et cetera).
 */
public interface IHasWork {
	/**
	 * Check if the Tile Entity is currently doing any work.
	 * @return True if the Tile Entity is doing work.
	 */
	boolean hasWork();
}
