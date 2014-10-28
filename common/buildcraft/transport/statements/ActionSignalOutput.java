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

import buildcraft.api.gates.IStatementParameter;
import buildcraft.api.gates.IGate;
import buildcraft.api.transport.PipeWire;
import buildcraft.core.statements.BCActionActive;
import buildcraft.core.statements.StatementIconProvider;
import buildcraft.core.utils.StringUtils;
import buildcraft.transport.Gate;

public class ActionSignalOutput extends BCActionActive {

	public PipeWire color;

	public ActionSignalOutput(PipeWire color) {
		super("buildcraft:pipe.wire.output." + color.name().toLowerCase(Locale.ENGLISH), "buildcraft.pipe.wire.output." + color.name().toLowerCase(Locale.ENGLISH));

		this.color = color;
	}

	@Override
	public String getDescription() {
		return String.format(StringUtils.localize("gate.action.pipe.wire"), StringUtils.localize("color." + color.name().toLowerCase(Locale.ENGLISH)));
	}

	@Override
	public int getIconIndex() {
		switch (color) {
			case RED:
				return StatementIconProvider.Trigger_PipeSignal_Red_Active;
			case BLUE:
				return StatementIconProvider.Trigger_PipeSignal_Blue_Active;
			case GREEN:
				return StatementIconProvider.Trigger_PipeSignal_Green_Active;
			case YELLOW:
			default:
				return StatementIconProvider.Trigger_PipeSignal_Yellow_Active;
		}
	}

	@Override
	public int maxParameters() {
		return 3;
	}

	@Override
	public IStatementParameter createParameter(int index) {
		return new ActionParameterSignal();
	}

	@Override
	public void actionActivate(IGate iGate, IStatementParameter[] parameters) {
		Gate gate = (Gate) iGate;

		gate.broadcastSignal(color);

		for (IStatementParameter param : parameters) {
			if (param != null) {
				ActionParameterSignal signal = (ActionParameterSignal) param;

				if (signal.color != null) {
					gate.broadcastSignal(signal.color);
				}
			}
		}
	}
}
