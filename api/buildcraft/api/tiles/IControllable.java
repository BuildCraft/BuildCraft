/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * The BuildCraft API is distributed under the terms of the MIT License.
 * Please check the contents of the license, which should be located
 * as "LICENSE.API" in the BuildCraft source code distribution.
 */
package buildcraft.api.tiles;

public interface IControllable {
	public enum Mode {
		Unknown, On, Off, Loop
	}

	Mode getControlMode();
	void setControlMode(Mode mode);
	boolean acceptsControlMode(Mode mode);
}
