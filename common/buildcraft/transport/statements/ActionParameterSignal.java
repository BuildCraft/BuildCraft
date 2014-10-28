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

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;
import buildcraft.api.core.NetworkData;
import buildcraft.api.gates.IActionParameter;
import buildcraft.api.gates.IStatement;
import buildcraft.api.gates.IStatementParameter;
import buildcraft.api.transport.IPipe;
import buildcraft.api.transport.IPipeTile;
import buildcraft.api.transport.PipeWire;
import buildcraft.core.statements.StatementIconProvider;
import buildcraft.core.utils.StringUtils;

public class ActionParameterSignal implements IActionParameter {

	@NetworkData
	public PipeWire color = null;
	private IIcon[] icons;
	
	public ActionParameterSignal() {

	}

	@Override
	public IIcon getIcon() {
		if (color == null) {
			return null;
		} else {
			return icons[color.ordinal() & 3];
		}
	}

	@Override
	public void onClick(Object source, IStatement stmt, ItemStack stack, int mouseButton) {
		if (color == null) {
			color = mouseButton == 0 ? PipeWire.RED : PipeWire.YELLOW;
		} else if (color == (mouseButton == 0 ? PipeWire.YELLOW : PipeWire.RED)) {
			color = null;
		} else {
			color = PipeWire.values()[mouseButton == 0 ? color.ordinal() + 1 : color.ordinal() - 1];
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
		return String.format(StringUtils.localize("gate.action.pipe.wire"), StringUtils.localize("color." + color.name().toLowerCase(Locale.ENGLISH)));
	}

	@Override
	public String getUniqueTag() {
		return "buildcraft:pipeWireAction";
	}

	@Override
	public void registerIcons(IIconRegister iconRegister) {
		icons = new IIcon[] {
				iconRegister.registerIcon("buildcraft:triggers/trigger_pipesignal_red_active"),
				iconRegister.registerIcon("buildcraft:triggers/trigger_pipesignal_blue_active"),
				iconRegister.registerIcon("buildcraft:triggers/trigger_pipesignal_green_active"),
				iconRegister.registerIcon("buildcraft:triggers/trigger_pipesignal_yellow_active")
		};

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
