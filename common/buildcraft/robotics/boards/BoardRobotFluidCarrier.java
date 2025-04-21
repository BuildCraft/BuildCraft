/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.robotics.boards;

import buildcraft.api.boards.RedstoneBoardRobot;
import buildcraft.api.boards.RedstoneBoardRobotNBT;
import buildcraft.api.core.IFluidFilter;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.lib.misc.CapUtil;
import buildcraft.robotics.ai.AIRobotGotoSleep;
import buildcraft.robotics.ai.AIRobotGotoStationAndLoadFluids;
import buildcraft.robotics.ai.AIRobotGotoStationAndUnloadFluids;
import buildcraft.robotics.statements.ActionRobotFilter;
import net.minecraftforge.fluids.FluidStack;

public class BoardRobotFluidCarrier extends RedstoneBoardRobot {

    public BoardRobotFluidCarrier(EntityRobotBase iRobot) {
        super(iRobot);
    }

    @Override
    public RedstoneBoardRobotNBT getNBTHandler() {
        return BCBoardNBT.REGISTRY.get("fluid_carrier");
    }

    @Override
    public void update() {
        if (!robotHasFluid()) {
            IFluidFilter filter = ActionRobotFilter.getGateFluidFilter(robot.getLinkedStation());
            startDelegateAI(new AIRobotGotoStationAndLoadFluids(robot, filter));
        } else {
            startDelegateAI(new AIRobotGotoStationAndUnloadFluids(robot));
        }
    }

    @Override
    public void delegateAIEnded(AIRobot ai) {
        if (ai instanceof AIRobotGotoStationAndLoadFluids) {
            if (!ai.success()) {
                startDelegateAI(new AIRobotGotoSleep(robot));
            }
        } else if (ai instanceof AIRobotGotoStationAndUnloadFluids) {
            if (!ai.success()) {
                startDelegateAI(new AIRobotGotoSleep(robot));
            }
        }
    }

    private boolean robotHasFluid() {
        // FluidStack tank = robot.getTankInfo(null)[0].fluid;
        FluidStack tank = robot.getCapability(CapUtil.CAP_FLUIDS).orElse(null).getFluidInTank(0);
        // return tank != null && tank.amount > 0;
        return !tank.isEmpty() && tank.getAmount() > 0;
    }
}
