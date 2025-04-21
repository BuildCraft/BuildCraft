/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.robotics.boards;

import buildcraft.api.core.IStackFilter;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.lib.misc.StackUtil;
import buildcraft.robotics.ai.AIRobotBreak;
import buildcraft.robotics.ai.AIRobotFetchAndEquipItemStack;
import buildcraft.robotics.ai.AIRobotGotoSleep;
import buildcraft.robotics.ai.AIRobotGotoStationAndUnload;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

public abstract class BoardRobotGenericBreakBlock extends BoardRobotGenericSearchBlock {

    public BoardRobotGenericBreakBlock(EntityRobotBase iRobot) {
        super(iRobot);
    }

    public abstract boolean isExpectedTool(@Nonnull ItemStack stack);

    @Override
    public final void update() {
        // if (!isExpectedTool(null) && robot.getHeldItem() == null)
        if (!isExpectedTool(StackUtil.EMPTY) && robot.getMainHandItem().isEmpty()) {
            startDelegateAI(new AIRobotFetchAndEquipItemStack(robot, new IStackFilter() {
                @Override
                public boolean matches(@Nonnull ItemStack stack) {
                    // return stack != null && (stack.getDamageValue() < stack.getMaxDamage()) && isExpectedTool(stack);
                    return !stack.isEmpty() && (stack.getDamageValue() < stack.getMaxDamage()) && isExpectedTool(stack);
                }
            }));
        }
        // else if (robot.getHeldItem() != null && robot.getHeldItem().getItemDamage() >= robot.getHeldItem().getMaxDamage())
        else if (!robot.getMainHandItem().isEmpty() && robot.getMainHandItem().getDamageValue() >= robot.getMainHandItem().getMaxDamage()) {
            startDelegateAI(new AIRobotGotoStationAndUnload(robot));
        } else if (blockFound() != null) {
            startDelegateAI(new AIRobotBreak(robot, blockFound()));
        } else {
            super.update();
        }
    }

    @Override
    public void delegateAIEnded(AIRobot ai) {
        if (ai instanceof AIRobotFetchAndEquipItemStack || ai instanceof AIRobotGotoStationAndUnload) {
            if (!ai.success()) {
                startDelegateAI(new AIRobotGotoSleep(robot));
            }
        } else if (ai instanceof AIRobotBreak) {
            releaseBlockFound(ai.success());
        }
        super.delegateAIEnded(ai);
    }
}
