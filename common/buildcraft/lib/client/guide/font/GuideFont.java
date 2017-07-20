/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.guide.font;

import java.awt.*;
import java.awt.image.BufferedImage;

public class GuideFont implements IFontRenderer {

    private final BufferedImage img;
    private final Graphics2D g2d;

    private final Font font;

    public GuideFont(Font font) {
        this.font = font;
        this.img = new BufferedImage(128, 128, BufferedImage.TYPE_4BYTE_ABGR);
        this.g2d = img.createGraphics();
        g2d.setFont(font);
    }

    @Override
    public int getStringWidth(String text) {
        return g2d.getFontMetrics().stringWidth(text);
    }

    @Override
    public int getFontHeight() {
        return g2d.getFontMetrics().getHeight();
    }

    @Override
    public int drawString(String text, int x, int y, int shade) {
        g2d.drawString(text, 0, 0);

        // Upload image

        return g2d.getFontMetrics().stringWidth(text);
    }
}
