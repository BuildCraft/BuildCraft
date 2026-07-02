package ct.buildcraft.robotics.ai;

import ct.buildcraft.api.boards.RedstoneBoardRobot;
import ct.buildcraft.api.robots.AIRobot;
import ct.buildcraft.api.robots.EntityRobotBase;

/** Main robotics AI loop, ported from BuildCraft 7.1.x. */
public class AIRobotMain extends AIRobot {
    private AIRobot overridingAI;
    private int rechargeCooldown;

    public AIRobotMain(EntityRobotBase robot) {
        super(robot);
    }

    @Override
    public int getEnergyCost() {
        return 0;
    }

    @Override
    public void preempt(AIRobot ai) {
        if (robot.getEnergy() <= EntityRobotBase.SHUTDOWN_ENERGY
                && (robot.getDockingStation() == null || !robot.getDockingStation().providesPower())) {
            if (!(ai instanceof AIRobotShutdown)) {
                startDelegateAI(new AIRobotShutdown(robot));
            }
        } else if (robot.getEnergy() < EntityRobotBase.SAFETY_ENERGY) {
            if (!(ai instanceof AIRobotRecharge) && !(ai instanceof AIRobotShutdown)) {
                if (rechargeCooldown-- <= 0) {
                    startDelegateAI(new AIRobotRecharge(robot));
                }
            }
        } else if (!(ai instanceof AIRobotRecharge)) {
            if (overridingAI != null && ai != overridingAI) {
                startDelegateAI(overridingAI);
            }
        }
    }

    @Override
    public void update() {
        RedstoneBoardRobot board = robot.getBoard();
        if (board != null) {
            startDelegateAI(board);
        }
    }

    @Override
    public void delegateAIEnded(AIRobot ai) {
        if (ai instanceof AIRobotRecharge && !ai.success()) {
            rechargeCooldown = 120;
        }
        if (ai == overridingAI) {
            overridingAI = null;
        }
    }

    public void setOverridingAI(AIRobot ai) {
        if (ai == null) {
            overridingAI = null;
        } else if (overridingAI == null) {
            overridingAI = ai;
        }
    }

    public AIRobot getOverridingAI() {
        return overridingAI;
    }

    @Override
    public boolean canLoadFromNBT() {
        return true;
    }
}
