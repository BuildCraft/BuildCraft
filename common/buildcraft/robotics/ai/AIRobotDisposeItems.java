/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.robotics.ai;

import buildcraft.api.core.IInvSlot;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.lib.inventory.InventoryIterator;
import buildcraft.lib.misc.CapUtil;
import buildcraft.lib.misc.StackUtil;
import net.minecraft.entity.item.ItemEntity;

public class AIRobotDisposeItems extends AIRobot {

    public AIRobotDisposeItems(EntityRobotBase iRobot) {
        super(iRobot);
    }

    @Override
    public void start() {
        startDelegateAI(new AIRobotGotoStationAndUnload(robot));
    }

    @Override
    public void delegateAIEnded(AIRobot ai) {
        if (ai instanceof AIRobotGotoStationAndUnload) {
            if (ai.success()) {
                if (robot.containsItems()) {
                    startDelegateAI(new AIRobotGotoStationAndUnload(robot));
                } else {
                    terminate();
                }
            } else {
                // for (IInvSlot slot : InventoryIterator.getIterable(robot))
                for (IInvSlot slot : InventoryIterator.getIterable(robot.getCapability(CapUtil.CAP_ITEMS).orElse(null))) {
                    // if (slot.getStackInSlot() != null)
                    if (!slot.getStackInSlot().isEmpty()) {
                        final ItemEntity entity = new ItemEntity(robot.level, robot.getX(), robot.getY(), robot.getZ(), slot.getStackInSlot());

                        robot.level.addFreshEntity(entity);

                        // slot.setStackInSlot(null);
                        slot.setStackInSlot(StackUtil.EMPTY);
                    }
                }
                terminate();
            }
        }
    }
}
