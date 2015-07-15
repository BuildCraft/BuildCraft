package buildcraft.robotics.statements;

import net.minecraft.item.ItemStack;

import buildcraft.api.items.IMapLocation;
import buildcraft.api.statements.IStatement;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.StatementMouseClick;
import buildcraft.api.statements.StatementParameterItemStack;

public class StatementParameterMapLocation extends StatementParameterItemStack {
	@Override
	public String getUniqueTag() {
		return "buildcraft:maplocation";
	}

	@Override
	public void onClick(IStatementContainer source, IStatement stmt, ItemStack stackIn, StatementMouseClick mouse) {
		ItemStack stack = stackIn;
		if (stack != null && !(stack.getItem() instanceof IMapLocation)) {
			stack = null;
		}
		super.onClick(source, stmt, stack, mouse);
	}
}
