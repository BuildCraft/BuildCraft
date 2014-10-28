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

import buildcraft.api.gates.IGate;
import buildcraft.api.gates.IStatementParameter;
import buildcraft.api.transport.PipeWire;
import buildcraft.core.statements.BCTrigger;
import buildcraft.core.statements.StatementIconProvider;
import buildcraft.core.utils.StringUtils;
import buildcraft.transport.Pipe;

public class TriggerPipeSignal extends BCTrigger {

	boolean active;
	PipeWire color;

	public TriggerPipeSignal(boolean active, PipeWire color) {
		super("buildcraft:pipe.wire.input." + color.name().toLowerCase(Locale.ENGLISH) + (active ? ".active" : ".inactive"),
				"buildcraft.pipe.wire.input." + color.name().toLowerCase(Locale.ENGLISH) + (active ? ".active" : ".inactive"));

		this.active = active;
		this.color = color;
	}

	@Override
	public int maxParameters() {
		return 3;
	}

	@Override
	public String getDescription() {
		return String.format(StringUtils.localize("gate.trigger.pipe.wire." + (active ? "active" : "inactive")), StringUtils.localize("color." + color.name().toLowerCase(Locale.ENGLISH)));
	}

	@Override
	public boolean isTriggerActive(IGate gate, IStatementParameter[] parameters) {
		Pipe<?> pipe = (Pipe<?>) gate.getPipe();

		if (active) {
			if (pipe.signalStrength[color.ordinal()] == 0) {
				return false;
			}
		} else {
			if (pipe.signalStrength[color.ordinal()] > 0) {
				return false;
			}
		}

		for (IStatementParameter param : parameters) {
			if (param != null) {
				TriggerParameterSignal signal = (TriggerParameterSignal) param;

				if (signal.color != null) {
					if (signal.active) {
						if (pipe.signalStrength[signal.color.ordinal()] == 0) {
							return false;
						}
					} else {
						if (pipe.signalStrength[signal.color.ordinal()] > 0) {
							return false;
						}
					}
				}
			}
		}

		return true;
	}

	@Override
	public int getIconIndex() {
		if (active) {
			switch (color) {
				case RED:
					return StatementIconProvider.Trigger_PipeSignal_Red_Active;
				case BLUE:
					return StatementIconProvider.Trigger_PipeSignal_Blue_Active;
				case GREEN:
					return StatementIconProvider.Trigger_PipeSignal_Green_Active;
				case YELLOW:
					return StatementIconProvider.Trigger_PipeSignal_Yellow_Active;
			}
		} else {
			switch (color) {
				case RED:
					return StatementIconProvider.Trigger_PipeSignal_Red_Inactive;
				case BLUE:
					return StatementIconProvider.Trigger_PipeSignal_Blue_Inactive;
				case GREEN:
					return StatementIconProvider.Trigger_PipeSignal_Green_Inactive;
				case YELLOW:
					return StatementIconProvider.Trigger_PipeSignal_Yellow_Inactive;
			}
		}

		return -1;
	}

	@Override
	public IStatementParameter createParameter(int index) {
		return new TriggerParameterSignal();
	}
}
