/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.robotics.ai;

import buildcraft.api.mj.MjAPI;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.lib.misc.BlockUtil;
import buildcraft.lib.misc.CapUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

public class AIRobotPumpBlock extends AIRobot {

    private BlockPos blockToPump;
    private long waited = 0;
    private int pumped = 0;

    public AIRobotPumpBlock(EntityRobotBase iRobot) {
        super(iRobot);
    }

    public AIRobotPumpBlock(EntityRobotBase iRobot, BlockPos iBlockToPump) {
        this(iRobot);

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
            FluidStack fluidStack = BlockUtil.drainBlock(robot.level, blockToPump, IFluidHandler.FluidAction.SIMULATE);
            if (fluidStack != null) {
//                if (robot.fill(null, fluidStack, true) > 0)
                if (robot.getCapability(CapUtil.CAP_FLUIDS).orElse(null).fill(fluidStack, IFluidHandler.FluidAction.EXECUTE) > 0) {
                    BlockUtil.drainBlock(robot.level, blockToPump, IFluidHandler.FluidAction.EXECUTE);
                }
            }
            terminate();
        }

    }

    @Override
    // public int getEnergyCost()
    public long getPowerCost() {
        return 5 * MjAPI.MJ / 10;
    }

    @Override
    public boolean success() {
        return pumped > 0;
    }
}
