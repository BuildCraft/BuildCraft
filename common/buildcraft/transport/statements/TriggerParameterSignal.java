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

public class TriggerParameterSignal implements IStatementParameter {

	public boolean active = false;
	public PipeWire color = null;

	public TriggerParameterSignal() {

	}

	@Override
	public ItemStack getItemStack() {
		return null;
	}

	@Override
	public SheetIcon getIcon() {
		if (color == null) {
			return null;
		}

		return new SheetIcon(BCStatement.STATEMENT_ICONS,
				15,
				7 + color.ordinal() * 2 + (active ? 1 : 0)
		);
	}

	@Override
	public void onClick(IStatementContainer source, IStatement stmt, ItemStack stack, StatementMouseClick mouse) {
		if (mouse.getButton() == 0) {
			if (color == null) {
				active = true;
				color = PipeWire.RED;
			} else if (active) {
				active = false;
			} else if (color == PipeWire.YELLOW) {
				color = null;
			} else {
				color = PipeWire.values()[color.ordinal() + 1];
				active = true;
			}
		} else {
			if (color == null) {
				active = false;
				color = PipeWire.YELLOW;
			} else if (!active) {
				active = true;
			} else if (color == PipeWire.RED) {
				color = null;
			} else {
				color = PipeWire.values()[color.ordinal() - 1];
				active = false;
			}
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		nbt.setBoolean("active", active);

		if (color != null) {
			nbt.setByte("color", (byte) color.ordinal());
		}

	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		active = nbt.getBoolean("active");

		if (nbt.hasKey("color")) {
			color = PipeWire.values()[nbt.getByte("color")];
		}
	}

	@Override
	public String getDescription() {
		if (color == null) {
			return null;
		}
		return String.format(StringUtils.localize("gate.trigger.pipe.wire." + (active ? "active" : "inactive")), StringUtils.localize("color." + color.name().toLowerCase(Locale.ENGLISH)));
	}

	@Override
	public String getUniqueTag() {
		return "buildcraft:pipeWireTrigger";
	}

	@Override
	public IStatementParameter rotateLeft() {
		return this;
	}
}
