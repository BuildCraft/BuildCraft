package buildcraft.robotics.statements;

import java.util.Collection;
import java.util.List;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import net.minecraft.item.ItemStack;

import buildcraft.api.boards.RedstoneBoardNBT;
import buildcraft.api.boards.RedstoneBoardRegistry;
import buildcraft.api.boards.RedstoneBoardRobotNBT;
import buildcraft.api.statements.IStatement;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.StatementMouseClick;
import buildcraft.api.statements.StatementParameterItemStack;
import buildcraft.robotics.ItemRobot;

public class StatementParameterRobot extends StatementParameterItemStack {

	@Override
	public void onClick(IStatementContainer source, IStatement stmt, ItemStack stack,
			StatementMouseClick mouse) {
		 if (stack == null && (this.stack == null || this.stack.getItem() instanceof ItemRobot)) {
			RedstoneBoardRobotNBT nextBoard = getNextBoard(mouse);
			if (nextBoard != null) {
				this.stack = ItemRobot.createRobotStack(nextBoard, 0);
			} else {
				this.stack = null;
			}
		} else {
			super.onClick(source, stmt, stack, mouse);
		}
	}

	private RedstoneBoardRobotNBT getNextBoard(StatementMouseClick mouse) {
		Collection<RedstoneBoardNBT<?>> boards = RedstoneBoardRegistry.instance.getAllBoardNBTs();
		if (this.stack == null || !(this.stack.getItem() instanceof ItemRobot)) {
			if (mouse.getButton() == 0) {
				return (RedstoneBoardRobotNBT) Iterables.getFirst(boards, null);
			} else {
				return (RedstoneBoardRobotNBT) Iterables.getLast(boards, null);
			}
		} else {
			if (mouse.getButton() > 0) {
				boards = Lists.reverse((List<RedstoneBoardNBT<?>>) boards);
			}
			boolean found = false;
			for (RedstoneBoardNBT boardNBT : boards) {
				if (found) {
					return (RedstoneBoardRobotNBT) boardNBT;
				} else if (ItemRobot.getRobotNBT(this.stack) == boardNBT) {
					found = true;
				}
			}
			return null;
		}
	}

	@Override
	public String getUniqueTag() {
		return "buildcraft:robot";
	}

}
