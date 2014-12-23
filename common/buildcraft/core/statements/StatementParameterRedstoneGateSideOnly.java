package buildcraft.core.statements;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import buildcraft.api.core.SheetIcon;
import buildcraft.api.statements.IStatement;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.StatementMouseClick;
import buildcraft.core.utils.StringUtils;

public class StatementParameterRedstoneGateSideOnly implements
		IStatementParameter {
	public boolean isOn = false;
	
	public StatementParameterRedstoneGateSideOnly() {
		
	}
	
	@Override
	public ItemStack getItemStack() {
		return null;
	}

	@Override
	public void onClick(IStatementContainer source, IStatement stmt, ItemStack stack, StatementMouseClick mouse) {
		isOn = !isOn;
	}

	@Override
	public void writeToNBT(NBTTagCompound compound) {
		compound.setByte("isOn", isOn ? (byte) 1 : (byte) 0);
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		if (compound.hasKey("isOn")) {
			isOn = compound.getByte("isOn") == 1;
		}
	}

	@Override
	public String getDescription() {
		return isOn ? StringUtils.localize("gate.parameter.redstone.gateSideOnly") : "";
	}

	@Override
	public String getUniqueTag() {
		return "buildcraft:redstoneGateSideOnly";
	}

	@Override
	public SheetIcon getIcon() {
		if (!isOn) {
			return null;
		} else {
			return new SheetIcon(BCStatement.STATEMENT_ICONS, 9, 0);
		}
	}

	@Override
	public IStatementParameter rotateLeft() {
		return this;
	}
}
