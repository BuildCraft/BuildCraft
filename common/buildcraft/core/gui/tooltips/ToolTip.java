/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.gui.tooltips;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ForwardingList;

public class ToolTip extends ForwardingList<ToolTipLine> {

	private final List<ToolTipLine> delegate = new ArrayList<ToolTipLine>();
	private final long delay;
	private long mouseOverStart;

	public ToolTip() {
		this.delay = 0;
	}

	public ToolTip(int delay) {
		this.delay = delay;
	}

	@Override
	protected final List<ToolTipLine> delegate() {
		return delegate;
	}

	public void onTick(boolean mouseOver) {
		if (delay == 0) {
			return;
		}
		if (mouseOver) {
			if (mouseOverStart == 0) {
				mouseOverStart = System.currentTimeMillis();
			}
		} else {
			mouseOverStart = 0;
		}
	}

	public boolean isReady() {
		if (delay == 0) {
			return true;
		}
		if (mouseOverStart == 0) {
			return false;
		}
		return System.currentTimeMillis() - mouseOverStart >= delay;
	}

	public void refresh() {
	}
}
