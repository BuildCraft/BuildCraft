/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.robotics.ai;

import buildcraft.api.core.IInvSlot;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.DockingStation;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.api.transport.IInjectable;
import buildcraft.lib.inventory.InventoryIterator;
import buildcraft.lib.inventory.filter.ArrayStackOrListFilter;
import buildcraft.lib.misc.CapUtil;
import buildcraft.lib.misc.StackUtil;
import buildcraft.robotics.statements.ActionRobotFilter;
import buildcraft.robotics.statements.ActionStationAcceptItems;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

public class AIRobotUnload extends AIRobot {

    private int waitedCycles = 0;

    public AIRobotUnload(EntityRobotBase iRobot) {
        super(iRobot);
    }

    @Override
    public void update() {
        waitedCycles++;

        if (waitedCycles > 40) {
            if (unload(robot, robot.getDockingStation(), true)) {
                waitedCycles = 0;
            } else {
                setSuccess(!robot.containsItems());
                terminate();
            }
        }
    }

    public static boolean unload(EntityRobotBase robot, DockingStation station, boolean doUnload) {
        if (station == null) {
            return false;
        }

        IInjectable output = station.getItemOutput();
        if (output == null) {
            return false;
        }

        Direction injectSide = station.getItemOutputSide().face.getOpposite();
        if (!output.canInjectItems(injectSide)) {
            return false;
        }

        // for (IInvSlot robotSlot : InventoryIterator.getIterable(robot))
        for (IInvSlot robotSlot : InventoryIterator.getIterable(robot.getCapability(CapUtil.CAP_ITEMS).orElse(null))) {
            // if (robotSlot.getStackInSlot() == null)
            if (robotSlot.getStackInSlot().isEmpty()) {
                continue;
            }

            if (!ActionRobotFilter.canInteractWithItem(station, new ArrayStackOrListFilter(robotSlot.getStackInSlot()),
                    ActionStationAcceptItems.class)) {
                continue;
            }

            ItemStack stack = robotSlot.getStackInSlot();
            // int used = output.injectItem(stack, doUnload, injectSide, null);
            ItemStack leftover = output.injectItem(stack, doUnload, injectSide, null, 0);

            // if (used > 0)
            if (stack.getCount() > leftover.getCount()) {
                if (doUnload) {
                    // robotSlot.decreaseStackInSlot(used);
                    robotSlot.setStackInSlot(leftover);
                }
                return true;
            }
        }

        // if (robot.getHeldItem() != null)
        if (!robot.getMainHandItem().isEmpty()) {
            // if (!ActionRobotFilter.canInteractWithItem(station, new ArrayStackOrListFilter(robot.getHeldItem()), ActionStationAcceptItems.class))
            if (!ActionRobotFilter.canInteractWithItem(station, new ArrayStackOrListFilter(robot.getMainHandItem()), ActionStationAcceptItems.class)) {
                return false;
            }

            // ItemStack stack = robot.getHeldItem();
            ItemStack stack = robot.getMainHandItem();
            // int used = output.injectItem(stack, doUnload, injectSide, null);
            ItemStack leftover = output.injectItem(stack, doUnload, injectSide, null, 0);

            // if (used > 0)
            if (stack.getCount() > leftover.getCount()) {
                if (doUnload) {
                    // if (stack.getCount() <= used)
                    if (leftover.getCount() <= 0) {
                        robot.setItemInUse(StackUtil.EMPTY);
                    } else {
                        // stack.shrink(used);
                        robot.setItemInHand(InteractionHand.MAIN_HAND, leftover);
                    }
                }
                return true;
            }
        }

        return false;
    }

    @Override
    // public int getEnergyCost()
    public long getPowerCost() {
        return 10 * MjAPI.MJ / 10;
    }
}
