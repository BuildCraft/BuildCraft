/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.robotics.ai;

import buildcraft.api.core.IInvSlot;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.api.robots.IRequestProvider;
import buildcraft.lib.inventory.filter.ArrayStackOrListFilter;
import buildcraft.lib.misc.CapUtil;
import buildcraft.lib.misc.InventoryUtil;
import buildcraft.robotics.StackRequest;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;

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
        if (requested != null) {
            startDelegateAI(new AIRobotGotoStation(robot, requested.getStation(robot.level)));
        } else {
            setSuccess(false);
            terminate();
        }
    }

    @Override
    public void delegateAIEnded(AIRobot ai) {
        if (ai instanceof AIRobotGotoStation) {
            if (!ai.success()) {
                setSuccess(false);
                terminate();
                return;
            }

            // IInvSlot slot = InvUtils.getItem(robot, new ArrayStackOrListFilter(requested.getStack()));
            IInvSlot slot = InventoryUtil.getItem(robot.getCapability(CapUtil.CAP_ITEMS).orElse(null), new ArrayStackOrListFilter(requested.getStack()));

            if (slot == null) {
                setSuccess(false);
                terminate();
                return;
            }

            IRequestProvider requester = requested.getRequester(robot.level);
            if (requester == null) {
                setSuccess(false);
                terminate();
                return;
            }
            ItemStack newStack = requester.offerItem(requested.getSlot(), slot.getStackInSlot().copy());

            // if (newStack == null || newStack.getCount() != slot.getStackInSlot().getCount())
            if (newStack.isEmpty() || newStack.getCount() != slot.getStackInSlot().getCount()) {
                slot.setStackInSlot(newStack);
            }
            terminate();
        }
    }

    @Override
    public boolean success() {
        return delivered;
    }

    @Override
    public boolean canLoadFromNBT() {
        return true;
    }

    @Override
    public void writeSelfToNBT(CompoundNBT nbt) {
        super.writeSelfToNBT(nbt);

        if (requested != null) {
            CompoundNBT requestNBT = new CompoundNBT();
            requested.writeToNBT(requestNBT);
            nbt.put("currentRequest", requestNBT);
        }
    }

    @Override
    public void loadSelfFromNBT(CompoundNBT nbt) {
        super.loadSelfFromNBT(nbt);
        if (nbt.contains("currentRequest")) {
            requested = StackRequest.loadFromNBT(nbt.getCompound("currentRequest"));
        }
    }
}
