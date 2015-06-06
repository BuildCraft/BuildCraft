/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.robotics.boards;

import java.util.ArrayList;

import net.minecraft.item.ItemStack;

import buildcraft.api.boards.RedstoneBoardRobot;
import buildcraft.api.boards.RedstoneBoardRobotNBT;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.api.robots.StackRequest;
import buildcraft.core.lib.inventory.StackHelper;
import buildcraft.core.lib.inventory.filters.IStackFilter;
import buildcraft.robotics.ai.AIRobotDeliverRequested;
import buildcraft.robotics.ai.AIRobotDisposeItems;
import buildcraft.robotics.ai.AIRobotGotoSleep;
import buildcraft.robotics.ai.AIRobotGotoStationAndLoad;
import buildcraft.robotics.ai.AIRobotSearchStackRequest;
import buildcraft.robotics.statements.ActionRobotFilter;

public class BoardRobotDelivery extends RedstoneBoardRobot {

    private ArrayList<ItemStack> deliveryBlacklist = new ArrayList<ItemStack>();

    private StackRequest currentRequest = null;

    public BoardRobotDelivery(EntityRobotBase iRobot) {
        super(iRobot);
    }

    @Override
    public RedstoneBoardRobotNBT getNBTHandler() {
        return BCBoardNBT.REGISTRY.get("delivery");
    }

    @Override
    public void update() {
        if (robot.containsItems()) {
            // Always makes sure that when starting a craft, the inventory is
            // clean.

            startDelegateAI(new AIRobotDisposeItems(robot));
            return;
        }

        if (currentRequest == null) {
            startDelegateAI(new AIRobotSearchStackRequest(robot, ActionRobotFilter.getGateFilter(robot.getLinkedStation()), deliveryBlacklist));
        } else {
            startDelegateAI(new AIRobotGotoStationAndLoad(robot, new ReqFilter(), 1));
        }
    }

    @Override
    public void delegateAIEnded(AIRobot ai) {
        if (ai instanceof AIRobotSearchStackRequest) {
            if (!ai.success()) {
                deliveryBlacklist.clear();
                startDelegateAI(new AIRobotGotoSleep(robot));
            } else {
                currentRequest = ((AIRobotSearchStackRequest) ai).request;

                if (!currentRequest.station.take(robot)) {
                    currentRequest = null;
                }
            }
        } else if (ai instanceof AIRobotGotoStationAndLoad) {
            if (!ai.success()) {
                deliveryBlacklist.add(currentRequest.stack);
                robot.releaseResources();
                currentRequest = null;
            } else {
                startDelegateAI(new AIRobotDeliverRequested(robot, currentRequest));
            }
        } else if (ai instanceof AIRobotDeliverRequested) {
            robot.releaseResources();
            currentRequest = null;
        }
    }

    private class ReqFilter implements IStackFilter {

        @Override
        public boolean matches(ItemStack stack) {
            if (currentRequest == null) {
                return false;
            } else {
                return StackHelper.isMatchingItemOrList(stack, currentRequest.stack);
            }
        }
    }
}
