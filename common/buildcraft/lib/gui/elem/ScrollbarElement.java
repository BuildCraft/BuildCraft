/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */
package buildcraft.lib.gui.elem;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.client.gui.DrawContext;

import buildcraft.lib.gui.BuildCraftGui;
import buildcraft.lib.gui.GuiElementSimple;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.IInteractionElement;
import buildcraft.lib.gui.pos.GuiRectangle;
import buildcraft.lib.gui.pos.IGuiPosition;
import buildcraft.lib.misc.MathUtil;

@Environment(EnvType.CLIENT)
public class ScrollbarElement extends GuiElementSimple implements IInteractionElement {
    private static final int HEIGHT = 14;
    private final GuiIcon background, scroller;
    private int pos, len;
    private boolean isClicking;

    public ScrollbarElement(BuildCraftGui gui, IGuiPosition parent, int height, GuiIcon background,
        GuiIcon scroller) {
        super(gui, new GuiRectangle(0, 0, 6, height).offset(parent));
        this.background = background;
        this.scroller = scroller;
    }

    @Override
    public void drawBackground(DrawContext context, float partialTicks) {
        if (len > 0) {
            background.drawAt(getX(), getY());
            double scrollY = getY() + pos * (getHeight() - HEIGHT + 2) / len;
            scroller.drawAt(getX(), scrollY);
        }
    }

    private void updatePositionFromMouse() {
        double h = getHeight();
        setPosition(((gui.mouse.getY() - getY()) * len + (h / 2)) / h);
    }

    @Override
    public void onMouseClicked(int button) {
        if (contains(gui.mouse) && button == 0) {
            isClicking = true;
            updatePositionFromMouse();
        }
    }

    @Override
    public void onMouseDragged(int button, long ticksSinceClick) {
        if (isClicking && button == 0) updatePositionFromMouse();
    }

    @Override
    public void onMouseReleased(int button) {
        if (isClicking && button == 0) {
            updatePositionFromMouse();
            isClicking = false;
        }
    }

    public int getPosition() { return pos; }

    public void setPosition(double pos) {
        this.pos = (int) MathUtil.clamp(pos, 0, len);
    }

    public void setLength(int len) {
        this.len = len;
        setPosition(this.pos);
    }
}
