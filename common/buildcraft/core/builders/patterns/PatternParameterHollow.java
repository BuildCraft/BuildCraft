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

public class PatternParameterHollow implements IStatementParameter {
	private static IIcon iconHollow, iconFilled;

	public boolean filled = false;

	public PatternParameterHollow() {
		super();
	}

	public PatternParameterHollow(boolean hollow) {
		this();
		this.filled = !hollow;
	}

	@Override
	public String getUniqueTag() {
		return "buildcraft:fillerParameterHollow";
	}

	@Override
	public IIcon getIcon() {
		return filled ? iconFilled : iconHollow;
	}

	@Override
	public ItemStack getItemStack() {
		return null;
	}

	@Override
	public void registerIcons(IIconRegister iconRegister) {
		iconFilled = iconRegister.registerIcon("buildcraftcore:fillerParameters/filled");
		iconHollow = iconRegister.registerIcon("buildcraftcore:fillerParameters/hollow");
	}

	@Override
	public String getDescription() {
		return StringUtils.localize("fillerpattern.parameter." + (filled ? "filled" : "hollow"));
	}

	@Override
	public void onClick(IStatementContainer source, IStatement stmt, ItemStack stack, StatementMouseClick mouse) {
		filled = !filled;
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		filled = compound.getBoolean("filled");
	}

	@Override
	public void writeToNBT(NBTTagCompound compound) {
		compound.setBoolean("filled", filled);
	}

	@Override
	public IStatementParameter rotateLeft() {
		return this;
	}
}
