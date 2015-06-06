/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.robotics.ai;

import net.minecraft.item.ItemStack;

import buildcraft.api.core.IInvSlot;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.api.robots.IRequestProvider;
import buildcraft.api.robots.StackRequest;
import buildcraft.core.lib.inventory.InvUtils;
import buildcraft.core.lib.inventory.filters.ArrayStackOrListFilter;

public class AIRobotDeliverRequested extends AIRobot {

    private StackRequest requested;
    private boolean delivered = false;

    public AIRobotDeliverRequested(EntityRobotBase iRobot) {
        super(iRobot);
    }

    public AIRobotDeliverRequested(EntityRobotBase robot, StackRequest request) {
        this(robot);

        requested = request;
    }

    @Override
    public void start() {
        startDelegateAI(new AIRobotGotoStation(robot, requested.station));
    }

    @Override
    public void delegateAIEnded(AIRobot ai) {
        if (ai instanceof AIRobotGotoStation) {
            if (!ai.success()) {
                terminate();
                return;
            }

            IInvSlot slot = InvUtils.getItem(robot, new ArrayStackOrListFilter(requested.stack));

            if (slot == null) {
                terminate();
                return;
            }

            if (requested.requester != null) {
                ItemStack newStack = ((IRequestProvider) requested.requester).provideItemsForRequest(requested.index, slot.getStackInSlot().copy());

                if (newStack == null || newStack.stackSize != slot.getStackInSlot().stackSize) {
                    delivered = true;
                    slot.setStackInSlot(newStack);
                }

                terminate();
            } else {
                startDelegateAI(new AIRobotUnload(robot));
                return;
            }
        } else if (ai instanceof AIRobotUnload) {
            delivered = ai.success();
            terminate();
        }
    }

    @Override
    public boolean success() {
        return delivered;
    }
}
