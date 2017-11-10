/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.lib.gui;

import java.util.List;

import net.minecraft.client.gui.FontRenderer;

import buildcraft.core.lib.gui.buttons.GuiBetterButton;

public final class GuiTools {

	/**
	 * Deactivate constructor
	 */
	private GuiTools() {
	}

	public static void drawCenteredString(FontRenderer fr, String s, int y) {
		drawCenteredString(fr, s, y, 176);
	}

	public static void drawCenteredString(FontRenderer fr, String s, int y, int guiWidth) {
		drawCenteredString(fr, s, y, guiWidth, 0x404040, false);
	}

	public static void drawCenteredString(FontRenderer fr, String s, int y, int guiWidth, int color, boolean shadow) {
		int sWidth = fr.getStringWidth(s);
		int sPos = guiWidth / 2 - sWidth / 2;
		fr.drawString(s, sPos, y, color, shadow);
	}

	public static void newButtonRowAuto(List<GuiBetterButton> buttonList, int xStart, int xSize, List<? extends GuiBetterButton> buttons) {
		int buttonWidth = 0;
		for (GuiBetterButton b : buttons) {
			buttonWidth += b.getWidth();
		}
		int remaining = xSize - buttonWidth;
		int spacing = remaining / (buttons.size() + 1);
		int pointer = 0;
		for (GuiBetterButton b : buttons) {
			pointer += spacing;
			b.xPosition = xStart + pointer;
			pointer += b.getWidth();
			buttonList.add(b);
		}
	}

	public static void newButtonRow(List<GuiBetterButton> buttonList, int xStart, int spacing, List<? extends GuiBetterButton> buttons) {
		int pointer = 0;
		for (GuiBetterButton b : buttons) {
			b.xPosition = xStart + pointer;
			pointer += b.getWidth() + spacing;
			buttonList.add(b);
		}
	}
}
