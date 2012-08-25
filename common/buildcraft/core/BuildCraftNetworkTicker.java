/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.core;

import java.util.Date;
import java.util.EnumSet;

import cpw.mods.fml.common.IScheduledTickHandler;
import cpw.mods.fml.common.TickType;

import buildcraft.BuildCraftCore;

public class BuildCraftNetworkTicker implements IScheduledTickHandler {
	long lastReport = 0;

	public boolean tick() {
		if (BuildCraftCore.trackNetworkUsage) {
			Date d = new Date();

			if (d.getTime() - lastReport > 10000) {
				lastReport = d.getTime();
				int bytes = ClassMapping.report();
				System.out.println("BuildCraft bandwidth = " + (bytes / 10) + " bytes / second");
				System.out.println();
			}
		}

		return true;
	}
	@Override
	public void tickStart(EnumSet<TickType> type, Object... tickData) {
	}

	@Override
	public void tickEnd(EnumSet<TickType> type, Object... tickData) {
		tick();
	}

	@Override
	public EnumSet<TickType> ticks() {
		return BuildCraftCore.trackNetworkUsage ? EnumSet.of(TickType.WORLD) : EnumSet.noneOf(TickType.class);
	}

	@Override
	public String getLabel() {
		return "BuildCraftNetworkTickMonitor";
	}

	@Override
	public int nextTickSpacing() {
		return 200;
	}

}
