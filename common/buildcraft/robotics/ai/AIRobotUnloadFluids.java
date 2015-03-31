/**
 * Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.robotics.ai;

import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.core.lib.inventory.filters.SimpleFluidFilter;
import buildcraft.robotics.DockingStation;
import buildcraft.robotics.statements.ActionRobotFilter;
import buildcraft.robotics.statements.ActionStationAcceptFluids;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeTransportFluids;

public class AIRobotUnloadFluids extends AIRobot {

	private int unloaded = 0;

	public AIRobotUnloadFluids(EntityRobotBase iRobot) {
		super(iRobot);
	}

	@Override
	public void update() {
		int previousUnloaded = unloaded;
		doLoad();

		if (unloaded == previousUnloaded) {
			terminate();
		}
	}

	private void doLoad() {
		if (robot.getDockingStation() != null) {
			DockingStation station = (DockingStation) robot.getDockingStation();

			if (!ActionRobotFilter.canInteractWithFluid(station,
					new SimpleFluidFilter(robot.getTankInfo(ForgeDirection.UNKNOWN)[0].fluid),
					ActionStationAcceptFluids.class)) {
				return;
			}

			if (((Pipe) station.getPipe().getPipe()).transport instanceof PipeTransportFluids) {
				PipeTransportFluids transport = (PipeTransportFluids) ((Pipe) station.getPipe().getPipe()).transport;
				FluidStack drainable = robot.drain(ForgeDirection.UNKNOWN, FluidContainerRegistry.BUCKET_VOLUME,
						false);

				if (drainable != null) {
					drainable = drainable.copy();

					int filled = transport.fill(station.side, drainable, true);

					if (filled > 0) {
						drainable.amount = filled;
						robot.drain(ForgeDirection.UNKNOWN, drainable, true);
						unloaded += filled;
						return;
					}
				}
			}
		}
	}

	@Override
	public int getEnergyCost() {
		return 10;
	}

	@Override
	public boolean success() {
		return unloaded > 0;
	}
}
