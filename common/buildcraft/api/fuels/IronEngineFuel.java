/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.api.fuels;

import com.google.common.collect.Maps;
import java.util.Map;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

public class IronEngineFuel {

	public static Map<String, IronEngineFuel> fuels = Maps.newHashMap();

	public static IronEngineFuel getFuelForFluid(Fluid liquid) {
		return liquid == null ? null : fuels.get(liquid.getName());
	}

	public final Fluid liquid;
	public final float powerPerCycle;
	public final int totalBurningTime;

	public IronEngineFuel(String fluidName, float powerPerCycle, int totalBurningTime) {
		this(FluidRegistry.getFluid(fluidName), powerPerCycle, totalBurningTime);
	}

	public IronEngineFuel(Fluid liquid, float powerPerCycle, int totalBurningTime) {
		this.liquid = liquid;
		this.powerPerCycle = powerPerCycle;
		this.totalBurningTime = totalBurningTime;
		fuels.put(liquid.getName(), this);
	}

	public static void addFuel(Fluid fluid, float powerPerCycle, int totalBurningTime) {
	    new IronEngineFuel(fluid, powerPerCycle, totalBurningTime);
	}
}
