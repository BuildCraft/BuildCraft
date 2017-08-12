/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.guide.font;

public interface IFontRenderer {
    int getStringWidth(String text);

    int getFontHeight();

    default int drawString(String text, int x, int y, int shade) {
        return drawString(text, x, y, shade, 1);
    }

    /** @param scale The scale, relative to {@link #getFontHeight()} */
    int drawString(String text, int x, int y, int shade, float scale);
}
