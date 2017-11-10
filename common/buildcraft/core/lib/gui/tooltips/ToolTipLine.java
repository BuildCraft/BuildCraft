/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.lib.gui.tooltips;

public class ToolTipLine {

	public String text;
	public final int color;
	public int spacing;

	public ToolTipLine(String text, int color) {
		this.text = text;
		this.color = color;
	}

	public ToolTipLine(String text) {
		this(text, -1);
	}

	public ToolTipLine() {
		this("", -1);
	}

	public void setSpacing(int spacing) {
		this.spacing = spacing;
	}

	public int getSpacing() {
		return spacing;
	}
}
