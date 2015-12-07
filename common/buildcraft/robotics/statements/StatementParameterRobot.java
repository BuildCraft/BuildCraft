package buildcraft.robotics.statements;

import net.minecraft.item.ItemStack;

import buildcraft.api.boards.RedstoneBoardRobotNBT;
import buildcraft.api.items.IList;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.api.statements.IStatement;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.StatementMouseClick;
import buildcraft.api.statements.StatementParameterItemStack;
import buildcraft.core.lib.inventory.StackHelper;
import buildcraft.robotics.EntityRobot;
import buildcraft.robotics.ItemRobot;
import buildcraft.robotics.RobotUtils;

public class StatementParameterRobot extends StatementParameterItemStack {

	@Override
	public void onClick(IStatementContainer source, IStatement stmt, ItemStack stack,
						StatementMouseClick mouse) {
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
					if (target != null && StackHelper.isMatchingItem(stack, target, true, true)) {
						return true;
					}
				}
			}
		}
		return false;
	}
}
