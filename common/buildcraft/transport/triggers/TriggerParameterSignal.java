/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport.triggers;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;

import buildcraft.api.core.NetworkData;
import buildcraft.api.gates.IStatement;
import buildcraft.api.gates.ITriggerParameter;
import buildcraft.api.transport.IPipeTile;
import buildcraft.api.transport.PipeWire;
import buildcraft.core.triggers.StatementIconProvider;

public class TriggerParameterSignal implements ITriggerParameter {
	@NetworkData
	private boolean active;

	@NetworkData
	private PipeWire color = PipeWire.RED;

	public TriggerParameterSignal() {
	}

	public TriggerParameterSignal(TriggerPipeSignal trigger) {
	}

	@Override
	public ItemStack getItemStackToDraw() {
		return null;
	}

	@Override
	public IIcon getIconToDraw() {
		int id = 0;

		if (active) {
			switch (color) {
			case RED:
				id = StatementIconProvider.Trigger_PipeSignal_Red_Active;
				break;
			case BLUE:
				id = StatementIconProvider.Trigger_PipeSignal_Blue_Active;
				break;
			case GREEN:
				id = StatementIconProvider.Trigger_PipeSignal_Green_Active;
				break;
			case YELLOW:
				id = StatementIconProvider.Trigger_PipeSignal_Yellow_Active;
				break;
			}
		} else {
			switch (color) {
			case RED:
				id = StatementIconProvider.Trigger_PipeSignal_Red_Inactive;
				break;
			case BLUE:
				id = StatementIconProvider.Trigger_PipeSignal_Blue_Inactive;
				break;
			case GREEN:
				id = StatementIconProvider.Trigger_PipeSignal_Green_Inactive;
				break;
			case YELLOW:
				id = StatementIconProvider.Trigger_PipeSignal_Yellow_Inactive;
				break;
			}
		}

		return StatementIconProvider.INSTANCE.getIcon(id);
	}

	@Override
	public void clicked(IPipeTile pipe, IStatement stmt, ItemStack stack) {
		if (stmt instanceof TriggerPipeSignal) {
			TriggerPipeSignal signal = (TriggerPipeSignal) stmt;

		}

	}

	@Override
	public void writeToNBT(NBTTagCompound compound) {
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
	}
}
