/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.robotics.ai;

import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.FluidStack;

import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.core.lib.utils.BlockUtils;

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
            FluidStack fluidStack = BlockUtils.drainBlock(robot.worldObj, blockToPump, false);
            if (fluidStack != null) {
                if (robot.fill(null, fluidStack, true) > 0) {
                    BlockUtils.drainBlock(robot.worldObj, blockToPump, true);
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
