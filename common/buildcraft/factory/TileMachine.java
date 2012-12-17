/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.factory;

import buildcraft.api.power.IPowerReceptor;
import buildcraft.core.IMachine;
import buildcraft.core.TileBuildCraft;

public abstract class TileMachine extends TileBuildCraft implements IMachine, IPowerReceptor {

	@Override
	public int powerRequest() {
		if (isActive())
			return (int) Math.ceil(Math.min(getPowerProvider().getMaxEnergyReceived(), getPowerProvider().getMaxEnergyStored()
					- getPowerProvider().getEnergyStored()));
		else
			return 0;
	}

}
