/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.lib.gui.pos;

/** An immutable {@link IGuiArea}. */
public final class GuiRectangle implements IGuiArea {
    /** A rectangle where all of the fields are set to 0. */
    public static final GuiRectangle ZERO = new GuiRectangle(0, 0, 0, 0);

    public final double x, y, width, height;

    public GuiRectangle(double x, double y, double width, double height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public GuiRectangle(double width, double height) {
        this.x = 0;
        this.y = 0;
        this.width = width;
        this.height = height;
    }

    @Override
    public double getX() {
        return x;
    }

    @Override
    public double getY() {
        return y;
    }

    @Override
    public double getWidth() {
        return width;
    }

    @Override
    public double getHeight() {
        return height;
    }

    @Override
    public GuiRectangle asImmutable() {
        return this;
    }

    @Override
    public String toString() {
        return "Rectangle [x=" + x + ", y=" + y + ", width=" + width + ", height=" + height + "]";
    }

    @Override
    public IGuiArea offset(IGuiPosition by) {
        if (by instanceof PositionAbsolute) {
            return offset(by.getX(), by.getY());
        }
        return IGuiArea.super.offset(by);
    }

    @Override
    public GuiRectangle offset(double dx, double dy) {
        return new GuiRectangle(x + dx, y + dy, width, height);
    }

    @Override
    public GuiRectangle expand(double dX, double dY) {
        return new GuiRectangle(x - dX, y - dY, width + dX * 2, height + dY * 2);
    }
}
