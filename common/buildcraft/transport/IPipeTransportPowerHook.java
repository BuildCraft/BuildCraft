/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport;

import net.minecraftforge.common.util.ForgeDirection;

public interface IPipeTransportPowerHook {

	/**
	 * Override default behavior on receiving energy into the pipe.
	 *
	 * @return The amount of power used, or -1 for default behavior.
	 */
	int receiveEnergy(ForgeDirection from, int val);

	/**
	 * Override default requested power.
	 */
	int requestEnergy(ForgeDirection from, int amount);
}
