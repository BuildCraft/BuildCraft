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
 * This interface should be implemented by any Tile Entity which wishes to
 * have non-redstone automation (for example, BuildCraft Gates, but also
 * other mods which implement it, e.g. OpenComputers).
 */
public interface IControllable {
	public enum Mode {
		Unknown, On, Off, Mode, Loop
	}

	/**
	 * Get the current control mode of the Tile Entity.
	 * @return
	 */
	Mode getControlMode();

	/**
	 * Set the mode of the Tile Entity.
	 * @param mode
	 */
	void setControlMode(Mode mode);

	/**
	 * Check if a given control mode is accepted.
	 * If you query IControllable tiles, you MUST check with
	 * acceptsControlMode first.
	 * @param mode
	 * @return True if this control mode is accepted.
	 */
	boolean acceptsControlMode(Mode mode);
}
