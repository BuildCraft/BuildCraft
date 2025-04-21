package buildcraft.robotics.statements;

import buildcraft.api.boards.RedstoneBoardRobotNBT;
import buildcraft.api.items.IList;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.api.statements.*;
import buildcraft.lib.misc.StackUtil;
import buildcraft.robotics.RobotUtils;
import buildcraft.robotics.entity.EntityRobot;
import buildcraft.robotics.item.ItemRobot;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

public class StatementParameterRobot extends StatementParameterItemStack {
    // Calen 1.18.2
    public static final StatementParameterRobot EMPTY;

    static {
        EMPTY = new StatementParameterRobot();
    }

    public StatementParameterRobot() {
        super();
    }

    public StatementParameterRobot(ItemStack itemStack) {
        super(itemStack);
    }

    public StatementParameterRobot(CompoundTag nbt) {
        super(nbt);
    }

    @Override
    // public void onClick(IStatementContainer source, IStatement stmt, ItemStack stack, StatementMouseClick mouse)
    public StatementParameterRobot onClick(IStatementContainer source, IStatement stmt, @Nonnull ItemStack stack, StatementMouseClick mouse) {
        // if (stack == null && (this.stack == null || this.stack.getItem() instanceof ItemRobot))
        if (stack.isEmpty() && (this.stack.isEmpty() || this.stack.getItem() instanceof ItemRobot)) {
            RedstoneBoardRobotNBT nextBoard = RobotUtils.getNextBoard(this.stack, mouse.getButton() > 0);
            if (nextBoard != null) {
                // this.stack = ItemRobot.createRobotStack(nextBoard, 0);
                return new StatementParameterRobot(ItemRobot.createRobotStack(nextBoard, 0));
            } else {
                // this.stack = null;
                return StatementParameterRobot.EMPTY;
            }
        }
//        else {
//            super.onClick(source, stmt, stack, mouse);
//        }
        else if (stack.isEmpty() || !(stack.getItem() instanceof ItemRobot)) {
            return EMPTY;
        } else {
            ItemStack newStack = stack.copy();
            newStack.setCount(1);
            return new StatementParameterRobot(newStack);
        }
        // return null;
    }

    @Override
    public IStatementParameter onScroll(IStatementContainer source, IStatement stmt, @NotNull ItemStack stack, double delta) {
        RedstoneBoardRobotNBT nextBoard = RobotUtils.getNextBoard(this.stack, delta > 0);
        if (nextBoard != null) {
            return new StatementParameterRobot(ItemRobot.createRobotStack(nextBoard, 0));
        } else {
            return StatementParameterRobot.EMPTY;
        }
    }

    @Override
    public String getUniqueTag() {
        return "buildcraft:robot";
    }

    public static boolean matches(IStatementParameter param, EntityRobotBase robot) {
        ItemStack stack = param.getItemStack();
        // if (stack != null)
        if (!stack.isEmpty()) {
            if (stack.getItem() instanceof IList) {
                IList list = (IList) stack.getItem();
                if (list.matches(stack, ItemRobot.createRobotStack(robot.getBoard().getNBTHandler(), robot.getPower()))) {
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
