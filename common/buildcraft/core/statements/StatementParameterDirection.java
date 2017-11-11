/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.statements;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;

import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.api.statements.IStatement;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.StatementMouseClick;
import buildcraft.api.transport.IPipeTile;
import buildcraft.core.lib.utils.StringUtils;

public class StatementParameterDirection implements IStatementParameter {

	private static IIcon[] icons;

	public ForgeDirection direction = ForgeDirection.UNKNOWN;

	public StatementParameterDirection() {

	}

	@Override
	public ItemStack getItemStack() {
		return null;
	}

	@Override
	public IIcon getIcon() {
		if (direction == ForgeDirection.UNKNOWN) {
			return null;
		} else {
			return icons[direction.ordinal()];
		}
	}

	@Override
	public void onClick(IStatementContainer source, IStatement stmt, ItemStack stack, StatementMouseClick mouse) {
		if (source.getTile() instanceof IPipeTile) {
			for (int i = 0; i < 6; i++) {
				int dir = (direction.ordinal() + (mouse.getButton() > 0 ? -1 : 1)) % 6;
				if (dir < 0) {
					dir += 6;
				}

				direction = ForgeDirection.getOrientation(dir);
				if (((IPipeTile) source.getTile()).isPipeConnected(direction)) {
					return;
				}
			}
			direction = ForgeDirection.UNKNOWN;
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		nbt.setByte("direction", (byte) direction.ordinal());
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		if (nbt.hasKey("direction")) {
			direction = ForgeDirection.getOrientation(nbt.getByte("direction"));
		} else {
			direction = ForgeDirection.UNKNOWN;
		}
	}

	@Override
	public boolean equals(Object object) {
		if (object instanceof StatementParameterDirection) {
			StatementParameterDirection param = (StatementParameterDirection) object;
			return param.direction == this.direction;
		}
		return false;
	}

	@Override
	public String getDescription() {
		if (direction == ForgeDirection.UNKNOWN) {
			return "";
		} else {
			return StringUtils.localize("direction." + direction.name().toLowerCase());
		}
	}

	@Override
	public String getUniqueTag() {
		return "buildcraft:pipeActionDirection";
	}

	@Override
	public void registerIcons(IIconRegister iconRegister) {
		icons = new IIcon[]{
				iconRegister.registerIcon("buildcraftcore:triggers/trigger_dir_down"),
				iconRegister.registerIcon("buildcraftcore:triggers/trigger_dir_up"),
				iconRegister.registerIcon("buildcraftcore:triggers/trigger_dir_north"),
				iconRegister.registerIcon("buildcraftcore:triggers/trigger_dir_south"),
				iconRegister.registerIcon("buildcraftcore:triggers/trigger_dir_west"),
				iconRegister.registerIcon("buildcraftcore:triggers/trigger_dir_east")
		};
	}

	@Override
	public IStatementParameter rotateLeft() {
		StatementParameterDirection d = new StatementParameterDirection();
		d.direction = direction.getRotation(ForgeDirection.UP);
		return d;
	}
}
