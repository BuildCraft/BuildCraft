/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.lib.gui.pos;

import java.util.Arrays;

public final class PositionAbsolute implements IGuiPosition {
    public static final PositionAbsolute ORIGIN = new PositionAbsolute(0, 0);

    private final double x, y;

    public PositionAbsolute(double x, double y) {
        this.x = x;
        this.y = y;
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
    public IGuiPosition offset(double xOffset, double yOffset) {
        return new PositionAbsolute(xOffset + x, yOffset + y);
    }

    @Override
    public IGuiPosition offset(IGuiPosition by) {
        return by.offset(x, y);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(new double[] { x, y });
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (obj.getClass() != getClass()) {
            return false;
        }
        PositionAbsolute other = (PositionAbsolute) obj;
        return x == other.x && y == other.y;
    }

    @Override
    public String toString() {
        return "{ " + x + ", " + y + " }";
    }
}
