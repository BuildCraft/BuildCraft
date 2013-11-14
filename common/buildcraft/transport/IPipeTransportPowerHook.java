/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.transport;

import net.minecraftforge.common.ForgeDirection;

public interface IPipeTransportPowerHook {

	/**
	 * Override default behavior on receiving energy into the pipe.
	 * 
	 * @return The amount of power used, or -1 for default behavior.
	 */
	public float receiveEnergy(ForgeDirection from, float val);

	/**
	 * Override default requested power.
	 */
	public float requestEnergy(ForgeDirection from, float amount);
}
