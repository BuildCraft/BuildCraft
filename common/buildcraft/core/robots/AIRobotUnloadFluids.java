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

import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidHandler;

import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.core.inventory.filters.SimpleFluidFilter;
import buildcraft.silicon.statements.ActionRobotFilter;
import buildcraft.silicon.statements.ActionStationAcceptFluids;

public class AIRobotUnloadFluids extends AIRobot {

	private int unloaded = 0;
	private int waitedCycles = 0;

	public AIRobotUnloadFluids(EntityRobotBase iRobot) {
		super(iRobot);
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

			if (!ActionRobotFilter.canInteractWithFluid(station,
					new SimpleFluidFilter(robot.getTankInfo(null)[0].fluid),
					ActionStationAcceptFluids.class)) {
				return;
			}

			for (EnumFacing dir : EnumFacing.values()) {
				TileEntity nearbyTile = robot.worldObj.getTileEntity(station.pos().offset(dir));

				if (nearbyTile != null && nearbyTile instanceof IFluidHandler) {
					IFluidHandler handler = (IFluidHandler) nearbyTile;

					FluidStack drainable = robot.drain(null, FluidContainerRegistry.BUCKET_VOLUME,
							false);

					if (drainable != null) {
						drainable = drainable.copy();

						int filled = handler.fill(station.side, drainable, true);

						if (filled > 0) {
							drainable.amount = filled;
							robot.drain(null, drainable, true);
							unloaded += filled;
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
		return unloaded > 0;
	}
}
