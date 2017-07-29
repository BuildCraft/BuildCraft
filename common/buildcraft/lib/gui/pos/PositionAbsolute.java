/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.gui.pos;

public final class PositionAbsolute implements IGuiPosition {
    private final int x, y;

    public PositionAbsolute(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public PositionAbsolute(long x, long y) {
        this((int) x, (int) y);
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getY() {
        return y;
    }

    @Override
    public IGuiPosition offset(int xOffset, int yOffset) {
        return new PositionAbsolute(xOffset + x, yOffset + y);
    }

    @Override
    public IGuiPosition offset(IGuiPosition by) {
        return by.offset(x, y);
    }

    @Override
    public int hashCode() {
        return Long.hashCode((((long) x) << 32) | y);
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
