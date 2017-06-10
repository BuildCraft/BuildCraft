/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.gui.pos;

public final class MousePosition implements IGuiPosition {
    private int x = -10, y = -10;

    public void setMousePosition(int mouseX, int mouseY) {
        this.x = mouseX;
        this.y = mouseY;
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
    public String toString() {
        return "Mouse [" + x + "," + y + "]";
    }
}
