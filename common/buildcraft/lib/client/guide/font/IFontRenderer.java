/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.guide.font;

import net.minecraft.client.gui.GuiGraphics;

import java.util.List;

public interface IFontRenderer {
    int getStringWidth(String text);

    int getFontHeight(String text);

    int getMaxFontHeight();

    default int drawString(GuiGraphics guiGraphics, String text, int x, int y, int colour) {
        return drawString(guiGraphics, text, x, y, colour, false, false, 1);
    }

    default int drawString(GuiGraphics guiGraphics, String text, int x, int y, int colour, boolean shadow, boolean centered) {
        return drawString(guiGraphics, text, x, y, colour, shadow, centered, 1);
    }

    /** @param scale The scale, relative to {@link #getFontHeight(String)} */
    int drawString(GuiGraphics guiGraphics, String text, int x, int y, int colour, boolean shadow, boolean centered, float scale);

    /** Breaks a string into a list of pieces where the width of each line is always less than or equal to the provided
     * width. Formatting codes will be preserved between lines. */
    List<String> wrapString(String text, int maxWidth, boolean shadow, float scale);
}
