/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.lib.gui.widgets;

import buildcraft.core.lib.gui.GuiBuildCraft;

public class ButtonWidget extends Widget {

	private boolean pressed;
	private int buttonPressed;

	public ButtonWidget(int x, int y, int u, int v, int w, int h) {
		super(x, y, u, v, w, h);
	}

	@Override
	public void draw(GuiBuildCraft gui, int guiX, int guiY, int mouseX, int mouseY) {
		int vv = pressed ? v + h : v;
		gui.drawTexturedModalRect(guiX + x, guiY + y, u, vv, w, h);
	}

	@Override
	public final boolean handleMouseClick(int mouseX, int mouseY, int mouseButton) {
		pressed = true;
		buttonPressed = mouseButton;
		onPress(buttonPressed);
		return true;
	}

	@Override
	public final void handleMouseRelease(int mouseX, int mouseY, int eventType) {
		if (pressed) {
			pressed = false;
			onRelease(buttonPressed);
		}
	}

	@Override
	public final void handleMouseMove(int mouseX, int mouseY, int mouseButton, long time) {
		if (pressed && !isMouseOver(mouseX, mouseY)) {
			pressed = false;
			onRelease(buttonPressed);
		}
	}

	public void onPress(int mouseButton) {
	}

	public void onRelease(int mouseButton) {
	}
}
