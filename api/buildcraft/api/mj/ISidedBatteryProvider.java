/**
 * Copyright (c) 2014, Prototik and the BuildFactory Team
 * http://buildfactory.org/
 *
 * BuildFactory is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://buildfactory.org/license
 */
package buildcraft.api.mj;

import net.minecraftforge.common.util.ForgeDirection;

public interface ISidedBatteryProvider extends IBatteryProvider {
	IBatteryObject getMjBattery(String kind, ForgeDirection direction);
}
