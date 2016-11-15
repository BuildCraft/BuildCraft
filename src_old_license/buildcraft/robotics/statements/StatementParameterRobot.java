package buildcraft.robotics.statements;

import net.minecraft.item.ItemStack;

import buildcraft.api.boards.RedstoneBoardRobotNBT;
import buildcraft.api.items.IList;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.api.statements.*;

import buildcraft.lib.misc.StackUtil;
import buildcraft.robotics.EntityRobot;
import buildcraft.robotics.ItemRobot;
import buildcraft.robotics.RobotUtils;

public class StatementParameterRobot extends StatementParameterItemStack {

    @Override
    public boolean onClick(IStatementContainer source, IStatement stmt, ItemStack stack, StatementMouseClick mouse) {
        if (stack == null && (this.stack == null || this.stack.getItem() instanceof ItemRobot)) {
            RedstoneBoardRobotNBT nextBoard = RobotUtils.getNextBoard(this.stack, mouse.getButton() > 0);
            if (nextBoard != null) {
                this.stack = ItemRobot.createRobotStack(nextBoard, 0);
            } else {
                this.stack = null;
            }
        } else {
            super.onClick(source, stmt, stack, mouse);
        }
        return true;
    }

    @Override
    public String getUniqueTag() {
        return "buildcraft:robot";
    }

    public static boolean matches(IStatementParameter param, EntityRobotBase robot) {
        ItemStack stack = param.getItemStack();
        if (stack != null) {
            if (stack.getItem() instanceof IList) {
                IList list = (IList) stack.getItem();
                if (list.matches(stack, ItemRobot.createRobotStack(robot.getBoard().getNBTHandler(), robot.getEnergy()))) {
                    return true;
                }
                for (ItemStack target : ((EntityRobot) robot).getWearables()) {
                    if (target != null && list.matches(stack, target)) {
                        return true;
                    }
                }
            } else if (stack.getItem() instanceof ItemRobot) {
                if (ItemRobot.getRobotNBT(stack) == robot.getBoard().getNBTHandler()) {
                    return true;
                }
            } else if (robot instanceof EntityRobot) {
                for (ItemStack target : ((EntityRobot) robot).getWearables()) {
                    if (target != null && StackUtil.isMatchingItem(stack, target, true, true)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
