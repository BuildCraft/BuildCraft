package ct.buildcraft.robotics.boards;

import ct.buildcraft.api.boards.RedstoneBoardRobot;
import ct.buildcraft.api.boards.RedstoneBoardRobotNBT;
import ct.buildcraft.api.core.IFluidFilter;
import ct.buildcraft.api.robots.AIRobot;
import ct.buildcraft.api.robots.EntityRobotBase;
import ct.buildcraft.robotics.BCRoboticsBoards;
import ct.buildcraft.robotics.ai.AIRobotGotoSleep;
import ct.buildcraft.robotics.ai.AIRobotGotoStationAndLoadFluids;
import ct.buildcraft.robotics.ai.AIRobotGotoStationAndUnloadFluids;
import ct.buildcraft.robotics.statements.ActionRobotFilter;
import net.minecraftforge.fluids.FluidStack;

public class BoardRobotFluidCarrier extends RedstoneBoardRobot {
    public BoardRobotFluidCarrier(EntityRobotBase robot) {
        super(robot);
    }

    @Override
    public RedstoneBoardRobotNBT getNBTHandler() {
        return BCRoboticsBoards.getByKey("fluid_carrier").nbt();
    }

    @Override
    public void update() {
        if (!robotHasFluid()) {
            IFluidFilter filter = ActionRobotFilter.getGateFluidFilter(robot.getLinkedStation());
            startDelegateAI(new AIRobotGotoStationAndLoadFluids(robot, filter));
        } else {
            startDelegateAI(new AIRobotGotoStationAndUnloadFluids(robot, true));
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
        } else if (ai instanceof AIRobotGotoSleep) {
            terminate();
        }
    }

    private boolean robotHasFluid() {
        FluidStack tank = robot.getFluidInTank(0);
        return !tank.isEmpty() && tank.getAmount() > 0;
    }
}
