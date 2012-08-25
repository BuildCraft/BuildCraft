/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.core;

import buildcraft.api.power.IPowerProvider;
import buildcraft.api.power.PowerFramework;

public class RedstonePowerFramework extends PowerFramework {

	@Override
	public IPowerProvider createPowerProvider() {
		return new RedstonePowerProvider();
	}

}
