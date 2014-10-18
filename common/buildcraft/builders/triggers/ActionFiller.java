/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.builders.triggers;

import net.minecraft.util.IIcon;
import buildcraft.core.builders.patterns.FillerPattern;
import buildcraft.core.triggers.BCActionPassive;

public class ActionFiller extends BCActionPassive {

	public final FillerPattern pattern;

	public ActionFiller(FillerPattern pattern) {
		super("filler:" + pattern.getUniqueTag());
		this.pattern = pattern;
	}

	@Override
	public String getDescription() {
		return "Pattern: " + pattern.getDisplayName();
	}

	@Override
	public IIcon getIcon() {
		return pattern.getIcon();
	}
}
