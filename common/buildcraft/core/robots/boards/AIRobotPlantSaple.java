/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.robots.boards;

import net.minecraft.item.ItemStack;
import net.minecraft.world.WorldServer;

import buildcraft.core.BlockIndex;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.robots.AIRobot;
import buildcraft.robots.EntityRobotBase;

public class AIRobotPlantSaple extends AIRobot {

	private BlockIndex toPlant;
	private int plantCycles = 0;

	public AIRobotPlantSaple(EntityRobotBase iRobot, BlockIndex index) {
		super(iRobot, 2);

		toPlant = index;
	}

	@Override
	public void start() {
		robot.aimItemAt(toPlant.x, toPlant.y, toPlant.z);
		robot.setItemActive(true);
	}

	@Override
	public void update() {
		plantCycles++;

		if (plantCycles > 40) {
			ItemStack stack = robot.getItemInUse();
			robot.setItemInUse(null);

			stack.getItem().onItemUse(stack, CoreProxy.proxy.getBuildCraftPlayer((WorldServer) robot.worldObj).get(),
					robot.worldObj, toPlant.x, toPlant.y + 1, toPlant.z, 0, 0, 0, 0);

			terminate();
		}
	}

	@Override
	public void end() {
		robot.setItemActive(false);
	}

}
