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

public class PatternParameterXZDir implements IStatementParameter {
	private static final String[] names = {
			"west", "east", "north", "south"
	};
	private static final int[] shiftLeft = {3, 2, 0, 1};
	private static final int[] shiftRight = {2, 3, 1, 0};
	private static IIcon[] icons;
	private int direction;

	public PatternParameterXZDir() {
		super();
	}

	public PatternParameterXZDir(int direction) {
		this();
		this.direction = direction;
	}

	@Override
	public String getUniqueTag() {
		return "buildcraft:fillerParameterXZDir";
	}

	@Override
	public IIcon getIcon() {
		return icons[direction & 3];
	}

	@Override
	public ItemStack getItemStack() {
		return null;
	}

	@Override
	public void registerIcons(IIconRegister iconRegister) {
		icons = new IIcon[]{
				iconRegister.registerIcon("buildcraftcore:fillerParameters/arrow_left"),
				iconRegister.registerIcon("buildcraftcore:fillerParameters/arrow_right"),
				iconRegister.registerIcon("buildcraftcore:fillerParameters/arrow_up"),
				iconRegister.registerIcon("buildcraftcore:fillerParameters/arrow_down")
		};
	}

	@Override
	public String getDescription() {
		return StringUtils.localize("direction." + names[direction & 3]);
	}

	@Override
	public void onClick(IStatementContainer source, IStatement stmt, ItemStack stack, StatementMouseClick mouse) {
		direction = shiftRight[direction & 3];
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		direction = compound.getByte("dir");
	}

	@Override
	public void writeToNBT(NBTTagCompound compound) {
		compound.setByte("dir", (byte) direction);
	}

	@Override
	public IStatementParameter rotateLeft() {
		return new PatternParameterXZDir(shiftLeft[direction & 3]);
	}

	public int getDirection() {
		return direction;
	}
}
