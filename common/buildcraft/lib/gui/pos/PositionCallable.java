/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.gui.pos;

import java.util.function.IntSupplier;

public class PositionCallable implements IGuiPosition {
    private final IntSupplier x, y;

    public PositionCallable(IntSupplier x, int y) {
        this(x, () -> y);
    }

    public PositionCallable(int x, IntSupplier y) {
        this(() -> x, y);
    }

    public PositionCallable(IntSupplier x, IntSupplier y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public int getX() {
        return x.getAsInt();
    }

    @Override
    public int getY() {
        return y.getAsInt();
    }
}
