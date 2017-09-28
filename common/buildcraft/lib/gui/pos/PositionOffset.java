/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.gui.pos;

public class PositionOffset implements IGuiPosition {
    public final IGuiPosition parent;
    public final double xOffset, yOffset;

    private PositionOffset(IGuiPosition parent, double xOffset, double yOffset) {
        this.parent = parent;
        this.xOffset = xOffset;
        this.yOffset = yOffset;
    }

    public static IGuiPosition createOffset(IGuiPosition from, double x, double y) {
        if (from == null) {
            return new PositionAbsolute(x, y);
        } else if (from instanceof PositionOffset) {
            PositionOffset parent = (PositionOffset) from;
            double oX = x + parent.xOffset;
            double oY = y + parent.yOffset;
            return parent.parent.offset(oX, oY);
        } else {
            return new PositionOffset(from, x, y);
        }
    }

    @Override
    public double getX() {
        return parent.getX() + xOffset;
    }

    @Override
    public double getY() {
        return parent.getY() + yOffset;
    }

    @Override
    public IGuiPosition offset(double x, double y) {
        return new PositionOffset(parent, x + xOffset, y + yOffset);
    }

    @Override
    public IGuiPosition offset(IGuiPosition by) {
        if (by instanceof PositionOffset) {
            PositionOffset other = (PositionOffset) by;
            return new PositionOffset(parent.offset(other.parent), xOffset + other.xOffset, yOffset + other.yOffset);
        } else {
            return IGuiPosition.super.offset(by);
        }
    }
}
