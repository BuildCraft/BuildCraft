/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.gui;

import buildcraft.api.core.render.ISprite;
import buildcraft.lib.gui.pos.GuiRectangle;
import buildcraft.lib.gui.pos.IGuiArea;
import com.mojang.blaze3d.matrix.MatrixStack;

/** An {@link ISimpleDrawable} that draws the specified {@link ISprite} as-is into the given width and height. */
public class GuiSpriteScaled implements ISimpleDrawable {
    public final ISprite sprite;
    public final IGuiArea area;

    public GuiSpriteScaled(ISprite sprite, IGuiArea area) {
        this.sprite = sprite;
        this.area = area;
    }

    public GuiSpriteScaled(ISprite sprite, double width, double height) {
        this(sprite, new GuiRectangle(width, height));
    }

    @Override
    public void drawAt(MatrixStack poseStack, double x, double y) {
        x += area.getX();
        y += area.getY();
        double x2 = x + area.getWidth();
        double y2 = y + area.getHeight();
        GuiIcon.draw(sprite, poseStack, x, y, x2, y2);
    }
}
