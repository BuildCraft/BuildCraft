/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.lib.gui.pos;

import java.util.function.DoubleSupplier;

public class PositionCallable implements IGuiPosition {
    private final DoubleSupplier x, y;

    public PositionCallable(DoubleSupplier x, double y) {
        this(x, () -> y);
    }

    public PositionCallable(double x, DoubleSupplier y) {
        this(() -> x, y);
    }

    public PositionCallable(DoubleSupplier x, DoubleSupplier y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public double getX() {
        return x.getAsDouble();
    }

    @Override
    public double getY() {
        return y.getAsDouble();
    }

    @Override
    public String toString() {
        return "{ " + x + ", " + y + " }";
    }
}
