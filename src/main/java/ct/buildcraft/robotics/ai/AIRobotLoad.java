package ct.buildcraft.robotics.ai;

import ct.buildcraft.api.core.IStackFilter;
import ct.buildcraft.api.robots.AIRobot;
import ct.buildcraft.api.robots.DockingStation;
import ct.buildcraft.api.robots.EntityRobotBase;
import ct.buildcraft.robotics.statements.ActionRobotFilter;
import ct.buildcraft.robotics.statements.ActionStationProvideItems;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.item.ItemStack;

public class AIRobotLoad extends AIRobot {
    public static final int ANY_QUANTITY = -1;

    private IStackFilter filter;
    private int quantity;
    private int waitedCycles;

    public AIRobotLoad(EntityRobotBase robot) {
        super(robot);
    }

    public AIRobotLoad(EntityRobotBase robot, IStackFilter filter, int quantity) {
        this(robot);
        this.filter = filter;
        this.quantity = quantity;
    }

    @Override
    public void update() {
        if (filter == null) {
            setSuccess(false);
            terminate();
            return;
        }

        waitedCycles++;
        if (waitedCycles > 40) {
            setSuccess(load(robot, robot.getDockingStation(), filter, quantity, true));
            terminate();
        }
    }

    public static ItemStack takeSingle(DockingStation station, IStackFilter filter, boolean doTake) {
        if (station == null || filter == null) {
            return ItemStack.EMPTY;
        }

        Container container = station.getItemInput();
        if (container == null) {
            return ItemStack.EMPTY;
        }

        Direction side = station.getItemInputSide();
        for (int slot : getSlots(container, side)) {
            ItemStack stack = container.getItem(slot);
            if (stack.isEmpty() || !canTake(container, slot, stack, side) || !filter.matches(stack)) {
                continue;
            }
            if (!canStationProvide(station, stack, filter)) {
                continue;
            }

            ItemStack result = stack.copy();
            result.setCount(1);
            if (doTake) {
                container.removeItem(slot, 1);
                container.setChanged();
            }
            return result;
        }
        return ItemStack.EMPTY;
    }

    public static boolean load(EntityRobotBase robot, DockingStation station, IStackFilter filter, int quantity, boolean doLoad) {
        if (robot == null || station == null || filter == null) {
            return false;
        }

        Container container = station.getItemInput();
        if (container == null) {
            return false;
        }

        int loaded = 0;
        Direction side = station.getItemInputSide();
        for (int slot : getSlots(container, side)) {
            ItemStack stack = container.getItem(slot);
            if (stack.isEmpty() || !canTake(container, slot, stack, side) || !filter.matches(stack)) {
                continue;
            }
            if (!canStationProvide(station, stack, filter)) {
                continue;
            }

            int toMove = quantity == ANY_QUANTITY ? stack.getCount() : Math.min(stack.getCount(), quantity - loaded);
            if (toMove <= 0) {
                return true;
            }

            ItemStack candidate = stack.copy();
            candidate.setCount(toMove);
            ItemStack remaining = insertIntoRobot(robot, candidate, doLoad);
            int moved = candidate.getCount() - remaining.getCount();
            if (moved <= 0) {
                continue;
            }

            if (doLoad) {
                container.removeItem(slot, moved);
                container.setChanged();
            }
            loaded += moved;

            if (quantity == ANY_QUANTITY || loaded >= quantity) {
                return true;
            }
        }
        return loaded > 0;
    }


    private static boolean canStationProvide(DockingStation station, ItemStack stack, IStackFilter requestedFilter) {
        return ActionStationProvideItems.canExtractItem(station, stack)
                && ActionRobotFilter.canInteractWithItem(station, requestedFilter, ActionStationProvideItems.class);
    }

    private static int[] getSlots(Container container, Direction side) {
        if (container instanceof WorldlyContainer sided && side != null) {
            return sided.getSlotsForFace(side);
        }
        int[] slots = new int[container.getContainerSize()];
        for (int i = 0; i < slots.length; i++) {
            slots[i] = i;
        }
        return slots;
    }

    private static boolean canTake(Container container, int slot, ItemStack stack, Direction side) {
        if (container instanceof WorldlyContainer sided && side != null) {
            return sided.canTakeItemThroughFace(slot, stack, side);
        }
        return true;
    }

    private static ItemStack insertIntoRobot(EntityRobotBase robot, ItemStack stack, boolean doInsert) {
        ItemStack remaining = stack.copy();

        for (int slot = 0; slot < robot.getContainerSize() && !remaining.isEmpty(); slot++) {
            ItemStack existing = robot.getItem(slot);
            if (!existing.isEmpty() && ItemStack.isSameItemSameTags(existing, remaining) && existing.isStackable()) {
                int limit = Math.min(existing.getMaxStackSize(), robot.getMaxStackSize());
                int move = Math.min(limit - existing.getCount(), remaining.getCount());
                if (move > 0) {
                    if (doInsert) {
                        existing.grow(move);
                        robot.setChanged();
                    }
                    remaining.shrink(move);
                }
            }
        }

        for (int slot = 0; slot < robot.getContainerSize() && !remaining.isEmpty(); slot++) {
            if (robot.getItem(slot).isEmpty() && robot.canPlaceItem(slot, remaining)) {
                int move = Math.min(Math.min(remaining.getMaxStackSize(), robot.getMaxStackSize()), remaining.getCount());
                if (doInsert) {
                    ItemStack inserted = remaining.copy();
                    inserted.setCount(move);
                    robot.setItem(slot, inserted);
                    robot.setChanged();
                }
                remaining.shrink(move);
            }
        }

        return remaining;
    }

    @Override
    public int getEnergyCost() {
        return 8;
    }
}
