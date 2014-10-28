package buildcraft.core.statements;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;
import buildcraft.api.core.NetworkData;
import buildcraft.api.statements.IStatement;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.transport.IPipeTile;
import buildcraft.core.utils.StringUtils;

public class StatementParameterRedstoneGateSideOnly implements
		IStatementParameter {
	@NetworkData
	public boolean isOn = false;
	
	private IIcon icon;
	
	public StatementParameterRedstoneGateSideOnly() {
		
	}
	
	@Override
	public ItemStack getItemStack() {
		return null;
	}

	@Override
	public IIcon getIcon() {
		if (!isOn) {
			return null;
		} else {
			return StatementIconProvider.INSTANCE.getIcon(StatementIconProvider.Action_Parameter_RedstoneGateSideOnly);
		}
	}

	@Override
	public void onClick(Object source, IStatement stmt, ItemStack stack, int mouseButton) {
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
	public void registerIcons(IIconRegister iconRegister) {
		icon = iconRegister.registerIcon("buildcraft:triggers/redstone_gate_side_only");
	}

	@Override
	public IStatementParameter rotateLeft() {
		return this;
	}
}
