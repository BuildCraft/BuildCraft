/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.gui.pos;

public class PositionAbsolute implements IGuiPosition {
    private final int x, y;

    public PositionAbsolute(int x, int y) {
        this.x = x;
        this.y = y;
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
}
