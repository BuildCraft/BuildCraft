/** 
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src.buildcraft.factory;

import net.minecraft.src.buildcraft.api.IPowerReceptor;
import net.minecraft.src.buildcraft.core.IMachine;
import net.minecraft.src.buildcraft.core.TileBuildCraft;

public abstract class TileMachine extends TileBuildCraft implements IMachine, IPowerReceptor {
	
	@Override
	public int powerRequest() {
		if (isActive()) {
			return getPowerProvider().maxEnergyReceived;
		} else {
			return 0;
		}		
	}
	
}
