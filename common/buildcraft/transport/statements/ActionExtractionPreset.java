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

import buildcraft.api.enums.EnumColor;
import buildcraft.api.statements.IActionInternal;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.core.statements.BCStatement;
import buildcraft.core.utils.StringUtils;

public class ActionExtractionPreset extends BCStatement implements IActionInternal {

	public final EnumColor color;

	public ActionExtractionPreset(EnumColor color) {
		super("buildcraft:extraction.preset." + color.getTag(), "buildcraft.extraction.preset." + color.getTag());

		this.color = color;
	}

	@Override
	public String getDescription() {
		return String.format(StringUtils.localize("gate.action.extraction"), color.getName());
	}

	@Override
	public int getSheetLocation() {
		return 13 + (color == EnumColor.BLUE ? 0 : (color == EnumColor.GREEN ? 1 : (color == EnumColor.RED ? 2 : 3)));
	}

	@Override
	public void actionActivate(IStatementContainer source,
			IStatementParameter[] parameters) {
		
	}
}
