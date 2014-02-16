/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.gui.widgets;

import buildcraft.core.gui.tooltips.ToolTip;
import buildcraft.core.gui.tooltips.ToolTipLine;

public abstract class IndicatorController implements IIndicatorController {

	private final ToolTip tips = new ToolTip() {
		@Override
		public void refresh() {
			refreshToolTip();
		}
	};
	protected ToolTipLine tip = new ToolTipLine();

	public IndicatorController() {
		tips.add(tip);
	}

	protected void refreshToolTip() {
	}

	@Override
	public final ToolTip getToolTip() {
		return tips;
	}
}
