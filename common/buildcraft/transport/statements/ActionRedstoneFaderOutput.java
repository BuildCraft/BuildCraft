/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport.statements;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import buildcraft.api.statements.IActionInternal;
import buildcraft.core.statements.ActionRedstoneOutput;
import buildcraft.core.utils.StringUtils;

public class ActionRedstoneFaderOutput extends ActionRedstoneOutput implements IActionInternal {

	public final int level;

	public ActionRedstoneFaderOutput(int level) {
		super(String.format("buildcraft:redstone.output.%02d", level));

		this.level = level;
	}

	@Override
	public String getDescription() {
		return String.format(StringUtils.localize("gate.trigger.redstone.input.level"), level);
	}

	@Override
	public int getSheetLocation() {
		return 9 + (level * 16);
	}

	@Override
	protected int getSignalLevel() {
		return level;
	}
}
