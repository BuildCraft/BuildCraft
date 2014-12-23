/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.robots;

import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import net.minecraft.util.BlockPos;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;

public class AIRobotPumpBlock extends AIRobot {

	public BlockPos blockToPump;
	public long waited = 0;
	int pumped = 0;

	public AIRobotPumpBlock(EntityRobotBase iRobot) {
		super(iRobot);
	}

	public AIRobotPumpBlock(EntityRobotBase iRobot, BlockPos iBlockToPump) {
		super(iRobot);

		blockToPump = iBlockToPump;
	}

	@Override
	public void start() {
		robot.aimItemAt(blockToPump);
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
			Fluid fluid = FluidRegistry.lookupFluidForBlock(robot.worldObj.getBlockState(blockToPump).getBlock());

			if (fluid != null) {
				pumped = robot.fill(null,
						new FluidStack(fluid, FluidContainerRegistry.BUCKET_VOLUME), true);

				if (pumped > 0) {
					robot.worldObj.setBlockToAir(blockToPump);
				}
			}

			terminate();
		}

	}

	@Override
	public void end() {
		robot.aimItemAt(new BlockPos(0, 1, 0));
	}

	@Override
	public int getEnergyCost() {
		return 20;
	}

	@Override
	public boolean success() {
		return pumped > 0;
	}
}
