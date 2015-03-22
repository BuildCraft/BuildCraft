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

import buildcraft.api.core.IInvSlot;
import buildcraft.api.core.IZone;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.api.transport.IPipe;
import buildcraft.core.lib.inventory.InventoryIterator;
import buildcraft.robotics.DockingStation;
import buildcraft.robotics.EntityRobot;
import buildcraft.robotics.IStationFilter;
import buildcraft.robotics.statements.ActionStationInputItems;
import buildcraft.transport.gates.ActionIterator;
import buildcraft.api.statements.StatementSlot;

public class AIRobotGotoStationToUnload extends AIRobot {

	private boolean found = false;
	private IZone zone;

	public AIRobotGotoStationToUnload(EntityRobotBase iRobot) {
		super(iRobot);
	}

	public AIRobotGotoStationToUnload(EntityRobotBase iRobot, IZone iZone) {
		super(iRobot);

		zone = iZone;

	}

	@Override
	public void start() {
		startDelegateAI(new AIRobotSearchAndGotoStation(robot, new StationInventory(), zone));
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

	private class StationInventory implements IStationFilter {
		@Override
		public boolean matches(DockingStation station) {
			IPipe pipe = station.getPipe().getPipe();

			for (IInvSlot robotSlot : InventoryIterator.getIterable(robot, ForgeDirection.UNKNOWN)) {
				if (robotSlot.getStackInSlot() == null) {
					continue;
				}

				for (StatementSlot s : new ActionIterator(pipe)) {
					if (s.statement instanceof ActionStationInputItems) {
						if (((ActionStationInputItems) s.statement).insert(station, (EntityRobot) robot, s, robotSlot, false)) {
							return true;
						}
					}
				}
			}

			return false;
		}
	}

}
