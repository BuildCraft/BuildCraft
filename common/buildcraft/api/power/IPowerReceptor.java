/**
 * Copyright (c) SpaceToad, 2011 http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License
 * 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.api.power;

import net.minecraftforge.common.ForgeDirection;

public interface IPowerReceptor {

	public void setPowerProvider(IPowerProvider provider);

	public IPowerProvider getPowerProvider();

	public void doWork();

	/**
	 * Used to request power from pipes. The return cannot be relied on to be
	 * anything more than a approximate guide to the power needed. When
	 * transferring power, you much check the return value of
	 * PowerProvider.receiverEnergy().
	 *
	 * @param from
	 * @return
	 */
	public int powerRequest(ForgeDirection from);
}
