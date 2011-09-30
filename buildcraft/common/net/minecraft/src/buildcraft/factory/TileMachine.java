/** 
 * BuildCraft is open-source. It is distributed under the terms of the 
 * BuildCraft Open Source License. It grants rights to read, modify, compile
 * or run the code. It does *NOT* grant the right to redistribute this software
 * or its modifications in any form, binary or source, except if expressively
 * granted by the copyright holder.
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
