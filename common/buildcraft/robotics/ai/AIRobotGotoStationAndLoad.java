/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.robotics.ai;

import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.core.lib.inventory.filters.IStackFilter;

public class AIRobotGotoStationAndLoad extends AIRobot {

    private IStackFilter filter;
    private int quantity;

    public AIRobotGotoStationAndLoad(EntityRobotBase iRobot) {
        super(iRobot);
    }

    public AIRobotGotoStationAndLoad(EntityRobotBase iRobot, IStackFilter iFilter, int iQuantity) {
        this(iRobot);

        filter = iFilter;
        quantity = iQuantity;
    }

    @Override
    public void start() {
        startDelegateAI(new AIRobotGotoStationToLoad(robot, filter, quantity));
    }

    @Override
    public void delegateAIEnded(AIRobot ai) {
        if (ai instanceof AIRobotGotoStationToLoad) {
            if (ai.success()) {
                startDelegateAI(new AIRobotLoad(robot, filter, quantity));
            } else {
                setSuccess(false);
                terminate();
            }
        } else if (ai instanceof AIRobotGotoStationToLoad) {
            setSuccess(ai.success());
            terminate();
        }
    }
}
