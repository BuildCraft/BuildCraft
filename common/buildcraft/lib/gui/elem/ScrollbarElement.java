/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.gui.elem;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.lib.gui.GuiBC8;
import buildcraft.lib.gui.GuiElementSimple;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.IInteractionElement;
import buildcraft.lib.gui.pos.GuiRectangle;
import buildcraft.lib.gui.pos.IGuiPosition;
import buildcraft.lib.misc.MathUtil;

@SideOnly(Side.CLIENT)
public class ScrollbarElement<G extends GuiBC8<?>> extends GuiElementSimple<G> implements IInteractionElement {
    private static final int HEIGHT = 14;
    private final GuiIcon background, scroller;
    private int pos, len;
    private boolean isClicking;

    public ScrollbarElement(G gui, IGuiPosition parent, int height, GuiIcon background, GuiIcon scroller) {
        super(gui, new GuiRectangle(0, 0, 6, height).offset(parent));
        this.background = background;
        this.scroller = scroller;
    }

    @Override
    public void drawBackground(float partialTicks) {
        if (len > 0) {
            background.drawAt(this);
            scroller.drawAt(this.offset(0, pos * (getHeight() - HEIGHT + 2) / len));
        }
    }

    private void updatePositionFromMouse() {
        double h = getHeight();
        setPosition(((gui.mouse.getY() - getY()) * len + (h / 2)) / h);
    }

    /** This is called EVEN IF the mouse is not inside your width and height! */
    @Override
    public void onMouseClicked(int button) {
        if (contains(gui.mouse)) {
            if (button == 0) {
                isClicking = true;
                updatePositionFromMouse();
            }
        }
    }

    /** This is called EVEN IF the mouse is not inside your width and height! */
    @Override
    public void onMouseDragged(int button, long ticksSinceClick) {
        if (isClicking && button == 0) {
            updatePositionFromMouse();
        }
    }

    /** This is called EVEN IF the mouse is not inside your width and height! */
    @Override
    public void onMouseReleased(int button) {
        if (isClicking && button == 0) {
            updatePositionFromMouse();
            isClicking = false;
        }
    }

    public int getPosition() {
        return pos;
    }

    public void setPosition(double pos) {
        this.pos = MathUtil.clamp(pos, 0, len);
    }

    public void setLength(int len) {
        this.len = len;
        setPosition(this.pos);
    }
}
