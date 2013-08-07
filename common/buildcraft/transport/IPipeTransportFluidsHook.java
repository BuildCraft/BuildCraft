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
import net.minecraftforge.fluids.FluidStack;

public interface IPipeTransportFluidsHook {

	public int fill(ForgeDirection from, FluidStack resource, boolean doFill);
}
