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
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import buildcraft.api.core.BlockIndex;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;

public class AIRobotPumpBlock extends AIRobot {

	public BlockIndex blockToPump;
	public long waited = 0;
	int pumped = 0;

	public AIRobotPumpBlock(EntityRobotBase iRobot) {
		super(iRobot);
	}

	public AIRobotPumpBlock(EntityRobotBase iRobot, BlockIndex iBlockToPump) {
		this(iRobot);

		blockToPump = iBlockToPump;
	}

	@Override
	public void start() {
		robot.aimItemAt(blockToPump.x, blockToPump.y, blockToPump.z);
	}

	@Override
	public void preempt(AIRobot ai) {
		super.preempt(ai);
	}

	@Override
	public void update() {
		if (waited < 40) {
			waited++;
		} else {
			Fluid fluid = FluidRegistry.lookupFluidForBlock(robot.worldObj.getBlock(blockToPump.x, blockToPump.y,
					blockToPump.z));

			if (fluid != null) {
				pumped = robot.fill(ForgeDirection.UNKNOWN,
						new FluidStack(fluid, FluidContainerRegistry.BUCKET_VOLUME), true);

				if (pumped > 0) {
					robot.worldObj.setBlockToAir(blockToPump.x, blockToPump.y, blockToPump.z);
				}
			}

			terminate();
		}

	}

	@Override
	public int getEnergyCost() {
		return 5;
	}

	@Override
	public boolean success() {
		return pumped > 0;
	}
}
