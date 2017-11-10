/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.lib.gui.buttons;

public class GuiToggleButton extends GuiBetterButton {

	public boolean active;

	public GuiToggleButton(int id, int x, int y, String label, boolean active) {
		this(id, x, y, 200, StandardButtonTextureSets.LARGE_BUTTON, label, active);
	}

	public GuiToggleButton(int id, int x, int y, int width, String s, boolean active) {
		super(id, x, y, width, StandardButtonTextureSets.LARGE_BUTTON, s);
		this.active = active;
	}

	public GuiToggleButton(int id, int x, int y, int width, IButtonTextureSet texture, String s, boolean active) {
		super(id, x, y, width, texture, s);
		this.active = active;
	}

	public void toggle() {
		active = !active;
	}

	@Override
	public int getHoverState(boolean mouseOver) {
		int state = 1;
		if (!enabled) {
			state = 0;
		} else if (mouseOver) {
			state = 2;
		} else if (!active) {
			state = 3;
		}
		return state;
	}

	@Override
	public int getTextColor(boolean mouseOver) {
		if (!enabled) {
			return 0xffa0a0a0;
		} else if (mouseOver) {
			return 0xffffa0;
		} else if (!active) {
			return 0x777777;
		} else {
			return 0xe0e0e0;
		}
	}
}
