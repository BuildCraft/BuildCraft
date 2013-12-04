/**
 * Copyright (c) SpaceToad, 2011 http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License
 * 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.builders.triggers;

import buildcraft.api.filler.IFillerPattern;
import buildcraft.core.triggers.*;
import net.minecraft.util.Icon;

public class ActionFiller extends BCAction {

	public final IFillerPattern pattern;

	public ActionFiller(IFillerPattern pattern) {
		super(0, "filler:" + pattern.getUniqueTag());
		this.pattern = pattern;
	}

	@Override
	public String getDescription() {
		return "Pattern: " + pattern.getDisplayName();
	}

	@Override
	public Icon getIcon() {
		return pattern.getIcon();
	}

	@Override
	public int getTextureMap() {
		return 0;
	}
}
