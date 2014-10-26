package buildcraft.core.statements;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;
import buildcraft.api.core.NetworkData;
import buildcraft.api.gates.IActionParameter;
import buildcraft.api.gates.IStatement;
import buildcraft.api.gates.ITriggerParameter;
import buildcraft.api.transport.IPipeTile;
import buildcraft.core.utils.StringUtils;

public class StatementParameterRedstoneGateSideOnly implements
		IActionParameter, ITriggerParameter {
	@NetworkData
	public boolean isOn = false;
	
	@Override
	public ItemStack getItemStackToDraw() {
		return null;
	}

	@Override
	public IIcon getIconToDraw() {
		if (!isOn) {
			return null;
		} else {
			return StatementIconProvider.INSTANCE.getIcon(StatementIconProvider.Action_Parameter_RedstoneGateSideOnly);
		}
	}

	@Override
	public void clicked(IPipeTile pipe, IStatement stmt, ItemStack stack, int mouseButton) {
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
}
