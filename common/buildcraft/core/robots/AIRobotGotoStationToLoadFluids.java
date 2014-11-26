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
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidHandler;

import buildcraft.api.core.IZone;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.core.inventory.filters.IFluidFilter;
import buildcraft.silicon.statements.ActionRobotFilter;
import buildcraft.silicon.statements.ActionStationProvideFluids;

public class AIRobotGotoStationToLoadFluids extends AIRobot {

	private boolean found = false;
	private IZone zone;
	private IFluidFilter filter;

	public AIRobotGotoStationToLoadFluids(EntityRobotBase iRobot) {
		super(iRobot);
	}

	public AIRobotGotoStationToLoadFluids(EntityRobotBase iRobot, IFluidFilter iFiler, IZone iZone) {
		super(iRobot);

		zone = iZone;
		filter = iFiler;
	}

	@Override
	public void update() {
		startDelegateAI(new AIRobotSearchAndGotoStation(robot, new StationFilter(), zone));
	}

	@Override
	public void delegateAIEnded(AIRobot ai) {
		if (ai instanceof AIRobotSearchAndGotoStation) {
			found = ai.success();

			terminate();
		}
	}

	@Override
	public boolean success() {
		return found;
	}

	private class StationFilter implements IStationFilter {

		@Override
		public boolean matches(DockingStation station) {
			if (!ActionRobotFilter.canInteractWithFluid(station, filter, ActionStationProvideFluids.class)) {
				return false;
			}

			for (EnumFacing dir : EnumFacing.values()) {
				TileEntity nearbyTile = robot.worldObj.getTileEntity(station.x() + dir.getFrontOffsetX(), station.y()
						+ dir.getFrontOffsetY(), station.z()
						+ dir.getFrontOffsetZ());

				if (nearbyTile != null && nearbyTile instanceof IFluidHandler) {
					IFluidHandler handler = (IFluidHandler) nearbyTile;
					FluidStack drainable = handler.drain(station.side, 1, false);

					// TODO: there is no account taken for the filter on the
					// gate here. See LoadFluid, GotoStationToLoad and Load for
					// items as well.
					if (drainable != null
							&& filter.matches(drainable.getFluid())
							&& robot.canFill(EnumFacing.UNKNOWN, drainable.getFluid())) {
						return true;
					}
				}
			}

			return false;
		}

	}
}
