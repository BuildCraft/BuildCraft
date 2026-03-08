/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.guide.parts;

import buildcraft.api.core.render.ISprite;
import buildcraft.lib.client.guide.GuiGuide;
import buildcraft.lib.gui.GuiSpriteScaled;
import com.mojang.blaze3d.vertex.PoseStack;

public class GuideImage extends GuidePart {
    public static final int PIXEL_HEIGHT = 42;
    final ISprite sprite;
    final GuiSpriteScaled icon, full;
    final int width, height;

    public GuideImage(GuiGuide gui, ISprite sprite, int srcWidth, int srcHeight, int width, int height) {
        super(gui);
        this.sprite = sprite;
/*
        int w = width;
        int h = height;
        if (h <= 0) {
            h = srcHeight;
        }
        if (w <= 0) {
            int sf = GuiGuide.PAGE_LEFT_TEXT.width / srcWidth;
            if (sf == 0) {
                int df = 1 + srcWidth / GuiGuide.PAGE_LEFT_TEXT.width;
                w = srcWidth / df;
                h /= df;
            } else {
                w = srcWidth * sf;
                h *= sf;
            }
        }
*/
        this.width = width;
        this.height = height;
        icon = new GuiSpriteScaled(sprite, width, height);
        full = new GuiSpriteScaled(sprite, srcWidth, srcHeight);
    }

    @Override
    public PagePosition renderIntoArea(PoseStack poseStack, int x, int y, int width, int height, PagePosition current, int index) {
        if (height - current.pixel < this.height) {
            current = current.nextPage();
        }
        if (index == current.page) {
            y += current.pixel;
            int w = this.width;
            int h = this.height;
            icon.drawAt(poseStack, x, y);

            GuiGuide.BORDER_TOP_LEFT.drawAt(poseStack, x, y);
            GuiGuide.BORDER_TOP_RIGHT.drawAt(poseStack, x + w - GuiGuide.BORDER_TOP_RIGHT.width, y);
            GuiGuide.BORDER_BOTTOM_LEFT.drawAt(poseStack, x, y + h - GuiGuide.BORDER_BOTTOM_LEFT.height);
            GuiGuide.BORDER_BOTTOM_RIGHT.drawAt(poseStack, x + w - GuiGuide.BORDER_BOTTOM_RIGHT.width, y + h - GuiGuide.BORDER_BOTTOM_RIGHT.height);
        }
        return current.nextLine(this.height + 1, height);
    }

    @Override
    public PagePosition handleMouseClick(PoseStack poseStack, int x, int y, int width, int height, PagePosition current, int index,
                                         double mouseX, double mouseY) {
        if (height - current.pixel < this.height) {
            current = current.nextPage();
        }
        if (index == current.page) {
            // icon.drawScaledInside(x, y + current.pixel, this.width, this.height);
        }
        return current.nextLine(this.height + 1, height);
    }
}
