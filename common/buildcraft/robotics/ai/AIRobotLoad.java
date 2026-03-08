/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.robotics.ai;

import buildcraft.api.core.IInvSlot;
import buildcraft.api.core.IStackFilter;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.DockingStation;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.lib.inventory.InventoryIterator;
import buildcraft.lib.misc.CapUtil;
import buildcraft.lib.misc.InventoryUtil;
import buildcraft.lib.misc.StackUtil;
import buildcraft.robotics.statements.ActionRobotFilter;
import buildcraft.robotics.statements.ActionStationProvideItems;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;

public class AIRobotLoad extends AIRobot {

    public static final int ANY_QUANTITY = -1;
    private IStackFilter filter;
    private int quantity;
    private int waitedCycles = 0;

    public AIRobotLoad(EntityRobotBase iRobot) {
        super(iRobot);
    }

    public AIRobotLoad(EntityRobotBase iRobot, IStackFilter iFilter, int iQuantity) {
        super(iRobot);

        filter = iFilter;
        quantity = iQuantity;
    }

    @Override
    public void update() {
        if (filter == null) {
            terminate();
            return;
        }

        waitedCycles++;

        if (waitedCycles > 40) {
            setSuccess(load(robot, robot.getDockingStation(), filter, quantity, true));
            terminate();
        }
    }

    /** Similar method to {@link #load(EntityRobotBase, DockingStation, IStackFilter, int, boolean)} but returns the
     * itemstack rather than loading it onto the robot.
     *
     * Only loads a single stack at once. */
    @Nonnull
    public static ItemStack takeSingle(DockingStation station, IStackFilter filter, boolean doTake) {
        if (station == null) {
            // return null;
            return StackUtil.EMPTY;
        }

        Container tileInventory = station.getItemInput();
        if (tileInventory == null) {
            // return null;
            return StackUtil.EMPTY;
        }

        for (IInvSlot slot : InventoryIterator.getIterable(tileInventory, station.getItemInputSide().face)) {
            ItemStack stack = slot.getStackInSlot();

            if (
                // stack == null//
                    stack.isEmpty()//
                            || !slot.canTakeStackFromSlot(stack)//
                            || !filter.matches(stack)//
                            || !ActionStationProvideItems.canExtractItem(station, stack)//
                            || !ActionRobotFilter.canInteractWithItem(station, filter, ActionStationProvideItems.class)
            ) {
                continue;
            }

            if (doTake) {
                stack = slot.decreaseStackInSlot(1);
            } else {
                stack = stack.copy();
                // stack = stack.splitStack(1);
                stack = stack.split(1);
            }
            return stack;
        }
        // return null;
        return StackUtil.EMPTY;
    }

    public static boolean load(EntityRobotBase robot, DockingStation station, IStackFilter filter, int quantity, boolean doLoad) {
        if (station == null) {
            return false;
        }

        int loaded = 0;

        Container tileInventory = station.getItemInput();
        if (tileInventory == null) {
            return false;
        }

        // for (IInvSlot slot : InventoryIterator.getIterable(tileInventory, station.getItemInputSide().face))
        for (IInvSlot slot : InventoryIterator.getIterable(tileInventory, station.getItemInputSide().face)) {
            ItemStack stack = slot.getStackInSlot();

            if (
                // stack == null ||
                    stack.isEmpty() ||
                            !slot.canTakeStackFromSlot(stack) ||
                            !filter.matches(stack) ||
                            !ActionStationProvideItems.canExtractItem(station, stack) ||
                            !ActionRobotFilter.canInteractWithItem(station, filter, ActionStationProvideItems.class)
            ) {
                continue;
            }

            // ITransactor robotTransactor = Transactor.getTransactorFor(robot);
            IItemHandler robotTransactor = robot.getCapability(CapUtil.CAP_ITEMS).orElse(null);

            if (quantity == ANY_QUANTITY) {
                ItemStack oldStack = slot.getStackInSlot();
                // ItemStack added = robotTransactor.add(slot.getStackInSlot(), null, doLoad);
                ItemStack overflow = InventoryUtil.insert(robotTransactor, oldStack, !doLoad);
                if (doLoad) {
                    // slot.decreaseStackInSlot(added.getCount());
                    slot.setStackInSlot(overflow);
                }
                // return added.getCount() > 0;
                return oldStack.getCount() > overflow.getCount();
            } else {
                ItemStack toAdd = slot.getStackInSlot().copy();

                if (toAdd.getCount() > quantity - loaded) {
                    toAdd.setCount(quantity - loaded);
                }

                // ItemStack added = robotTransactor.add(toAdd, null, doLoad);
                ItemStack overflow = InventoryUtil.insert(robotTransactor, toAdd, !doLoad);
                ItemStack added = toAdd.copy();
                added.shrink(overflow.getCount());
                if (doLoad) {
                    slot.decreaseStackInSlot(added.getCount());
                }
                // loaded += added.stackSize;
                loaded += added.getCount();

                if (quantity - loaded <= 0) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    // public int getEnergyCost()
    public long getPowerCost() {
        return 8 * MjAPI.MJ / 10;
    }
}
