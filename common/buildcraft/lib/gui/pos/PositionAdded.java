/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.gui.pos;

public class PositionAdded implements IGuiPosition {
    private final IGuiPosition a, b;

    public PositionAdded(IGuiPosition a, IGuiPosition b) {
        this.a = a;
        this.b = b;
    }

    @Override
    public double getX() {
        return a.getX() + b.getX();
    }

    @Override
    public double getY() {
        return a.getY() + b.getY();
    }
}
