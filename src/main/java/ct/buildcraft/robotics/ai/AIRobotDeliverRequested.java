package ct.buildcraft.robotics.ai;

import ct.buildcraft.api.robots.AIRobot;
import ct.buildcraft.api.robots.EntityRobotBase;
import ct.buildcraft.api.robots.IRequestProvider;
import ct.buildcraft.lib.inventory.filter.ArrayStackOrListFilter;
import ct.buildcraft.robotics.StackRequest;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

/** Delivers the robot inventory into the station that made a request. */
public class AIRobotDeliverRequested extends AIRobot {
    private StackRequest requested;
    private boolean delivered;

    public AIRobotDeliverRequested(EntityRobotBase robot) {
        super(robot);
    }

    public AIRobotDeliverRequested(EntityRobotBase robot, StackRequest request) {
        this(robot);
        this.requested = request;
    }

    @Override
    public void start() {
        if (requested != null && requested.getStation(robot.level) != null) {
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

            IRequestProvider requester = requested == null ? null : requested.getRequester(robot.level);
            if (requester == null) {
                setSuccess(false);
                terminate();
                return;
            }

            delivered = deliver(requester);
            setSuccess(delivered);
            terminate();
        }
    }

    private boolean deliver(IRequestProvider requester) {
        if (requested == null || requested.getStack().isEmpty()) {
            return false;
        }

        ArrayStackOrListFilter filter = new ArrayStackOrListFilter(requested.getStack());
        int remainingRequest = Math.max(1, requested.getStack().getCount());
        int deliveredCount = 0;

        for (int slot = 0; slot < robot.getContainerSize() && remainingRequest > 0; slot++) {
            ItemStack stack = robot.getItem(slot);
            if (stack.isEmpty() || !filter.matches(stack)) {
                continue;
            }

            int toOffer = Math.min(stack.getCount(), remainingRequest);
            ItemStack offered = stack.copy();
            offered.setCount(toOffer);
            ItemStack rejected = requester.offerItem(requested.getSlot(), offered.copy());
            int rejectedCount = rejected == null || rejected.isEmpty() ? 0 : rejected.getCount();
            int accepted = toOffer - rejectedCount;
            if (accepted <= 0) {
                continue;
            }

            robot.removeItem(slot, accepted);
            deliveredCount += accepted;
            remainingRequest -= accepted;
        }

        return deliveredCount > 0;
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
    public void writeSelfToNBT(CompoundTag nbt) {
        if (requested != null) {
            CompoundTag requestTag = new CompoundTag();
            requested.writeToNBT(requestTag);
            nbt.put("currentRequest", requestTag);
        }
    }

    @Override
    public void loadSelfFromNBT(CompoundTag nbt) {
        if (nbt.contains("currentRequest")) {
            requested = StackRequest.loadFromNBT(nbt.getCompound("currentRequest"));
        }
    }
}
