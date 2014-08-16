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
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidHandler;

import buildcraft.api.core.IZone;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.core.inventory.filters.StatementParameterStackFilter;
import buildcraft.silicon.statements.ActionStationProvideFluids;
import buildcraft.transport.Pipe;
import buildcraft.transport.gates.ActionIterator;
import buildcraft.transport.gates.ActionSlot;

public class AIRobotGotoStationToLoadFluids extends AIRobot {

	private boolean found = false;
	private IZone zone;

	public AIRobotGotoStationToLoadFluids(EntityRobotBase iRobot) {
		super(iRobot);
	}

	public AIRobotGotoStationToLoadFluids(EntityRobotBase iRobot, IZone iZone) {
		super(iRobot);

		zone = iZone;
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
			boolean actionFound = false;

			Pipe pipe = station.getPipe().pipe;

			for (ActionSlot s : new ActionIterator(pipe)) {
				if (s.action instanceof ActionStationProvideFluids) {
					StatementParameterStackFilter param = new StatementParameterStackFilter(s.parameters);

					/*
					 * if (!param.hasFilter() || param.matches(filter)) {
					 * actionFound = true; break; }
					 */

					actionFound = true;
					break;
				}
			}

			if (!actionFound) {
				return false;
			}

			for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
				TileEntity nearbyTile = robot.worldObj.getTileEntity(station.x() + dir.offsetX, station.y()
						+ dir.offsetY, station.z()
						+ dir.offsetZ);

				if (nearbyTile != null && nearbyTile instanceof IFluidHandler) {
					IFluidHandler handler = (IFluidHandler) nearbyTile;
					FluidStack drainable = handler.drain(station.side, 1, false);

					if (robot.canFill(ForgeDirection.UNKNOWN, drainable.getFluid())) {
						return true;
					}
				}
			}

			return false;
		}

	}
}
