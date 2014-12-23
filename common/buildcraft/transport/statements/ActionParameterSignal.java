/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport.statements;

import java.util.Locale;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import buildcraft.api.core.SheetIcon;
import buildcraft.api.statements.IStatement;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.StatementMouseClick;
import buildcraft.api.transport.PipeWire;
import buildcraft.core.statements.BCStatement;
import buildcraft.core.utils.StringUtils;

public class ActionParameterSignal implements IStatementParameter {

	public PipeWire color = null;

	public ActionParameterSignal() {

	}

	@Override
	public void onClick(IStatementContainer source, IStatement stmt, ItemStack stack, StatementMouseClick mouse) {
		if (color == null) {
			color = mouse.getButton() == 0 ? PipeWire.RED : PipeWire.YELLOW;
		} else if (color == (mouse.getButton() == 0 ? PipeWire.YELLOW : PipeWire.RED)) {
			color = null;
		} else {
			color = PipeWire.values()[mouse.getButton() == 0 ? color.ordinal() + 1 : color.ordinal() - 1];
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		if (color != null) {
			nbt.setByte("color", (byte) color.ordinal());
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		if (nbt.hasKey("color")) {
			color = PipeWire.values()[nbt.getByte("color")];
		}
	}

	@Override
	public boolean equals(Object object) {
		if (object instanceof ActionParameterSignal) {
			ActionParameterSignal param = (ActionParameterSignal) object;

			return param.color == color;
		} else {
			return false;
		}
	}
	
	@Override
	public String getDescription() {
		if (color == null) {
			return null;
		}
		return String.format(StringUtils.localize("gate.action.pipe.wire"), StringUtils.localize("color." + color.name().toLowerCase(Locale.ENGLISH)));
	}

	@Override
	public String getUniqueTag() {
		return "buildcraft:pipeWireAction";
	}

	@Override
	public SheetIcon getIcon() {
		if (color != null) {
			return new SheetIcon(BCStatement.STATEMENT_ICONS, 15, 7 + (color.ordinal() * 2));
		}
		return null;
	}

	@Override
	public IStatementParameter rotateLeft() {
		return this;
	}

	@Override
	public ItemStack getItemStack() {
		return null;
	}
}
