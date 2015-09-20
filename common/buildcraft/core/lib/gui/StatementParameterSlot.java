package buildcraft.core.lib.gui;

import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;

import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.StatementParameterItemStack;

/**
 * Created by asie on 1/24/15.
 */
public abstract class StatementParameterSlot extends AdvancedSlot {
	public int slot;
	public StatementSlot statementSlot;

	public StatementParameterSlot(GuiAdvancedInterface gui, int x, int y, int slot, StatementSlot iStatementSlot) {
		super(gui, x, y);

		this.slot = slot;
		this.statementSlot = iStatementSlot;
		statementSlot.parameters.add(this);
	}

	@Override
	public boolean isDefined() {
		return getParameter() != null;
	}

	@Override
	public String getDescription() {
		IStatementParameter parameter = getParameter();

		// HACK: We're explicitly returning null so that the item stack description is used.
		if (parameter != null && !(parameter instanceof StatementParameterItemStack)) {
			return parameter.getDescription() != null ? parameter.getDescription() : "";
		} else {
			return null;
		}
	}

	@Override
	public ItemStack getItemStack() {
		IStatementParameter parameter = getParameter();

		if (parameter != null) {
			return parameter.getItemStack();
		} else {
			return null;
		}
	}

	@Override
	public IIcon getIcon() {
		IStatementParameter parameter = getParameter();

		if (parameter != null) {
			return parameter.getIcon();
		} else {
			return null;
		}
	}

	public abstract IStatementParameter getParameter();

	public boolean isAllowed() {
		return statementSlot.getStatement() != null && slot < statementSlot.getStatement().maxParameters();
	}

	public boolean isRequired() {
		return statementSlot.getStatement() != null && slot < statementSlot.getStatement().minParameters();
	}

	public abstract void setParameter(IStatementParameter param, boolean notifyServer);
}
