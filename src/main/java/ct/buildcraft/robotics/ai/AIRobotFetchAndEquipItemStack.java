package ct.buildcraft.robotics.ai;

import ct.buildcraft.api.core.IStackFilter;
import ct.buildcraft.api.robots.AIRobot;
import ct.buildcraft.api.robots.DockingStation;
import ct.buildcraft.api.robots.EntityRobotBase;
import ct.buildcraft.robotics.IStationFilter;
import ct.buildcraft.lib.inventory.filter.AggregateFilter;
import ct.buildcraft.robotics.statements.ActionRobotFilterTool;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

/** Fetches a matching stack from a station-side inventory and equips it as the robot held item. */
public class AIRobotFetchAndEquipItemStack extends AIRobot {
    private IStackFilter filter;
    private int maxStackSize = 1;
    private int delay;

    public AIRobotFetchAndEquipItemStack(EntityRobotBase robot) {
        super(robot);
    }

    public AIRobotFetchAndEquipItemStack(EntityRobotBase robot, IStackFilter filter) {
        this(robot, filter, 1);
    }

    public AIRobotFetchAndEquipItemStack(EntityRobotBase robot, IStackFilter filter, int maxStackSize) {
        this(robot);
        this.filter = filter == null ? null : new AggregateFilter(ActionRobotFilterTool.getGateFilter(robot.getLinkedStation()), filter);
        this.maxStackSize = Math.max(1, maxStackSize);
    }

    @Override
    public void start() {
        if (filter == null) {
            setSuccess(false);
            terminate();
            return;
        }
        if (robot.getDockingStation() == null || !canTakeSingle(robot.getDockingStation(), filter)) {
            startDelegateAI(new AIRobotSearchAndGotoStation(robot, new StationToolFilter(), robot.getZoneToLoadUnload()));
        }
    }

    @Override
    public void update() {
        if (filter == null || robot.getDockingStation() == null) {
            setSuccess(false);
            terminate();
            return;
        }
        if (delay++ > 40) {
            ItemStack stack = takeMatching(robot.getDockingStation(), filter, maxStackSize, true);
            if (!stack.isEmpty()) {
                robot.setItemInUse(stack);
                terminate();
            } else {
                delay = 0;
                startDelegateAI(new AIRobotSearchAndGotoStation(robot, new StationToolFilter(), robot.getZoneToLoadUnload()));
            }
        }
    }

    @Override
    public void delegateAIEnded(AIRobot ai) {
        if (ai instanceof AIRobotSearchAndGotoStation && !ai.success()) {
            setSuccess(false);
            terminate();
        }
    }

    private boolean canTakeSingle(DockingStation station, IStackFilter filter) {
        return !takeMatching(station, filter, maxStackSize, false).isEmpty();
    }

    private static ItemStack takeMatching(DockingStation station, IStackFilter filter, int maxCount, boolean doTake) {
        if (station == null || filter == null) return ItemStack.EMPTY;
        Container inventory = station.getItemInput();
        if (inventory == null) return ItemStack.EMPTY;

        int remaining = Math.max(1, maxCount);
        ItemStack result = ItemStack.EMPTY;

        for (int slot = 0; slot < inventory.getContainerSize() && remaining > 0; slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack.isEmpty() || !filter.matches(stack)) continue;

            if (result.isEmpty()) {
                result = stack.copy();
                result.setCount(0);
            } else if (!ItemStack.isSameItemSameTags(result, stack)) {
                continue;
            }

            int toTake = Math.min(remaining, stack.getCount());
            result.grow(toTake);
            remaining -= toTake;

            if (doTake) {
                inventory.removeItem(slot, toTake);
            }
        }

        if (doTake && !result.isEmpty()) {
            inventory.setChanged();
        }
        return result;
    }

    private class StationToolFilter implements IStationFilter {
        @Override
        public boolean matches(DockingStation station) {
            return canTakeSingle(station, filter);
        }
    }
}
