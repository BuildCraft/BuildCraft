/**
 * Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.robotics.ai;

import net.minecraft.tileentity.TileEntity;

import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidHandler;

import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.core.inventory.filters.IFluidFilter;
import buildcraft.robotics.DockingStation;
import buildcraft.robotics.statements.ActionRobotFilter;
import buildcraft.robotics.statements.ActionStationProvideFluids;

public class AIRobotLoadFluids extends AIRobot {

	private int loaded = 0;
	private int waitedCycles = 0;
	private IFluidFilter filter;

	public AIRobotLoadFluids(EntityRobotBase iRobot) {
		super(iRobot);
	}

	public AIRobotLoadFluids(EntityRobotBase iRobot, IFluidFilter iFilter) {
		super(iRobot);

		filter = iFilter;
	}

	@Override
	public void update() {
		waitedCycles++;

		if (waitedCycles > 40) {
			int previousLoaded = loaded;
			doLoad();

			if (loaded == previousLoaded) {
				terminate();
			} else {
				waitedCycles = 0;
			}
		}
	}

	private void doLoad() {
		if (robot.getDockingStation() != null) {
			DockingStation station = (DockingStation) robot.getDockingStation();

			if (!ActionRobotFilter.canInteractWithFluid(station, filter, ActionStationProvideFluids.class)) {
				return;
			}

			for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
				TileEntity nearbyTile = robot.worldObj.getTileEntity(station.x() + dir.offsetX, station.y()
						+ dir.offsetY, station.z()
						+ dir.offsetZ);

				if (nearbyTile != null && nearbyTile instanceof IFluidHandler) {
					IFluidHandler handler = (IFluidHandler) nearbyTile;
					FluidStack drainable = handler.drain(station.side, FluidContainerRegistry.BUCKET_VOLUME, false);

					if (drainable != null
							&& filter.matches(drainable.getFluid())) {

						drainable = drainable.copy();

						int filled = robot.fill(ForgeDirection.UNKNOWN, drainable, true);

						if (filled > 0) {
							drainable.amount = filled;
							handler.drain(station.side, drainable, true);
							loaded += filled;
							return;
						}
					}
				}
			}
		}
	}

	@Override
	public int getEnergyCost() {
		return 20;
	}

	@Override
	public boolean success() {
		return loaded > 0;
	}
}
