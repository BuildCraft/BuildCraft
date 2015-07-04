package buildcraft.core.builders.patterns;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;

import buildcraft.api.statements.IStatement;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.StatementMouseClick;
import buildcraft.core.lib.utils.StringUtils;

public class PatternParameterYDir implements IStatementParameter {
	private static IIcon iconUp, iconDown;

	public boolean up = false;

	public PatternParameterYDir() {
		super();
	}

	public PatternParameterYDir(boolean up) {
		this();
		this.up = up;
	}

	@Override
	public String getUniqueTag() {
		return "buildcraft:fillerParameterYDir";
	}

	@Override
	public IIcon getIcon() {
		return up ? iconUp : iconDown;
	}

	@Override
	public ItemStack getItemStack() {
		return null;
	}

	@Override
	public void registerIcons(IIconRegister iconRegister) {
		iconUp = iconRegister.registerIcon("buildcraftcore:fillerParameters/stairs_ascend");
		iconDown = iconRegister.registerIcon("buildcraftcore:fillerParameters/stairs_descend");
	}

	@Override
	public String getDescription() {
		return StringUtils.localize("direction." + (up ? "up" : "down"));
	}

	@Override
	public void onClick(IStatementContainer source, IStatement stmt, ItemStack stack, StatementMouseClick mouse) {
		up = !up;
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		up = compound.getBoolean("up");
	}

	@Override
	public void writeToNBT(NBTTagCompound compound) {
		compound.setBoolean("up", up);
	}

	@Override
	public IStatementParameter rotateLeft() {
		return this;
	}
}
