/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.robotics.boards;

import buildcraft.api.boards.RedstoneBoardRobot;
import buildcraft.api.boards.RedstoneBoardRobotNBT;
import buildcraft.api.core.IStackFilter;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.lib.misc.StackUtil;
import buildcraft.robotics.StackRequest;
import buildcraft.robotics.ai.*;
import buildcraft.robotics.statements.ActionRobotFilter;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;

import java.util.ArrayList;

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
            startDelegateAI(new AIRobotDisposeItems(robot));
            return;
        }

        if (currentRequest == null) {
            startDelegateAI(new AIRobotSearchStackRequest(robot, ActionRobotFilter.getGateFilter(robot.getLinkedStation()), deliveryBlacklist));
        } else {
            startDelegateAI(new AIRobotGotoStationAndLoad(robot, new IStackFilter() {
                @Override
                public boolean matches(ItemStack stack) {
                    return currentRequest != null && StackUtil.isMatchingItemOrList(stack, currentRequest.getStack());
                }
            }, currentRequest.getStack().getCount()));
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

                if (!currentRequest.getStation(robot.level).take(robot)) {
                    releaseCurrentRequest();
                }
            }
        } else if (ai instanceof AIRobotGotoStationAndLoad) {
            if (!ai.success()) {
                deliveryBlacklist.add(currentRequest.getStack());
                releaseCurrentRequest();
            } else {
                startDelegateAI(new AIRobotDeliverRequested(robot, currentRequest));
            }
        } else if (ai instanceof AIRobotDeliverRequested) {
            releaseCurrentRequest();
        }
    }

    private void releaseCurrentRequest() {
        if (currentRequest != null) {
            robot.getRegistry().release(currentRequest.getResourceId(robot.level));
            currentRequest.getStation(robot.level).release(robot);
            currentRequest = null;
        }
    }

    @Override
    public boolean canLoadFromNBT() {
        return true;
    }

    @Override
    public void writeSelfToNBT(CompoundNBT nbt) {
        super.writeSelfToNBT(nbt);

        if (currentRequest != null) {
            CompoundNBT requestNBT = new CompoundNBT();
            currentRequest.writeToNBT(requestNBT);
            nbt.put("currentRequest", requestNBT);
        }
    }

    @Override
    public void loadSelfFromNBT(CompoundNBT nbt) {
        super.loadSelfFromNBT(nbt);
        if (nbt.contains("currentRequest")) {
            currentRequest = StackRequest.loadFromNBT(nbt.getCompound("currentRequest"));
        }
    }
}
