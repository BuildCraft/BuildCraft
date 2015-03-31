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
import net.minecraftforge.fluids.FluidStack;
import buildcraft.api.core.IZone;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.core.lib.inventory.filters.SimpleFluidFilter;
import buildcraft.robotics.DockingStation;
import buildcraft.robotics.IStationFilter;
import buildcraft.robotics.statements.ActionRobotFilter;
import buildcraft.robotics.statements.ActionStationAcceptFluids;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeTransportFluids;

public class AIRobotGotoStationToUnloadFluids extends AIRobot {

	private boolean found = false;
	private IZone zone;

	public AIRobotGotoStationToUnloadFluids(EntityRobotBase iRobot) {
		super(iRobot);
	}

	public AIRobotGotoStationToUnloadFluids(EntityRobotBase iRobot, IZone iZone) {
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
			found = ((AIRobotSearchAndGotoStation) ai).targetStation != null;

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
			if (!ActionRobotFilter.canInteractWithFluid(station,
					new SimpleFluidFilter(robot.getTankInfo(ForgeDirection.UNKNOWN)[0].fluid),
					ActionStationAcceptFluids.class)) {
				return false;
			}

			if (((Pipe) station.getPipe().getPipe()).transport instanceof PipeTransportFluids) {
				PipeTransportFluids transport = (PipeTransportFluids) ((Pipe) station.getPipe().getPipe()).transport;
				FluidStack drainable = robot.drain(ForgeDirection.UNKNOWN, 1, false);

				int filledAmount = transport.fill(station.side, drainable, false);

				if (filledAmount > 0) {
					return true;
				}
			}

			return false;
		}

	}
}
