/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.robots;

import net.minecraft.tileentity.TileEntity;

import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidHandler;

import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.core.inventory.filters.IFluidFilter;
import buildcraft.silicon.statements.ActionRobotFilter;
import buildcraft.silicon.statements.ActionStationAcceptFluids;

public class AIRobotUnloadFluids extends AIRobot {

	private int unloaded = 0;
	private int waitedCycles = 0;
	private IFluidFilter filter;

	public AIRobotUnloadFluids(EntityRobotBase iRobot) {
		super(iRobot);
	}

	public AIRobotUnloadFluids(EntityRobotBase iRobot, IFluidFilter iFilter) {
		super(iRobot);

		filter = iFilter;
	}

	@Override
	public void update() {
		waitedCycles++;

		if (waitedCycles > 40) {
			int previousUnloaded = unloaded;
			doLoad();

			if (unloaded == previousUnloaded) {
				terminate();
			} else {
				waitedCycles = 0;
			}
		}
	}

	private void doLoad() {
		if (robot.getDockingStation() != null) {
			DockingStation station = (DockingStation) robot.getDockingStation();

			if (!ActionRobotFilter.canInteractWithFluid(station, filter, ActionStationAcceptFluids.class)) {
				return;
			}

			for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
				TileEntity nearbyTile = robot.worldObj.getTileEntity(station.x() + dir.offsetX, station.y()
						+ dir.offsetY, station.z()
						+ dir.offsetZ);

				if (nearbyTile != null && nearbyTile instanceof IFluidHandler) {
					IFluidHandler handler = (IFluidHandler) nearbyTile;

					FluidStack drainable = robot.drain(ForgeDirection.UNKNOWN, FluidContainerRegistry.BUCKET_VOLUME,
							false);

					if (drainable != null && filter.matches(drainable.getFluid())) {
						drainable = drainable.copy();

						int filled = handler.fill(station.side, drainable, true);

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
	}

	@Override
	public double getEnergyCost() {
		return 2;
	}

	@Override
	public boolean success() {
		return unloaded > 0;
	}
}
