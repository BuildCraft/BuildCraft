/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.robots;

import java.util.LinkedList;

public class RobotAIComposite extends RobotAIBase {

	LinkedList<RobotAIBase> cpts = new LinkedList<RobotAIBase>();

	public RobotAIComposite(EntityRobot iRobot, RobotAIBase... icpts) {
		super(iRobot);

		for (RobotAIBase ai : icpts) {
			cpts.add(ai);
		}
	}

	@Override
	public void updateTask() {
		if (cpts.size() > 0) {
			if (cpts.getFirst().isDone()) {
				cpts.removeFirst();
			} else {
				cpts.getFirst().updateTask();
			}
		}
	}

	@Override
	public boolean shouldExecute() {
		if (cpts.size() > 0) {
			return cpts.getFirst().shouldExecute();
		} else {
			return true;
		}
	}

	@Override
	public boolean isDone() {
		return cpts.size() == 0;
	}
}
