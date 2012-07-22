/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src.buildcraft.core;

import net.minecraft.src.buildcraft.api.power.IPowerProvider;
import net.minecraft.src.buildcraft.api.power.PowerFramework;
import net.minecraft.src.buildcraft.api.power.PowerProvider;

public class RedstonePowerFramework extends PowerFramework {

	@Override
	public IPowerProvider createPowerProvider() {
		return new RedstonePowerProvider();
	}

}
