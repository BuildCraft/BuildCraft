/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.gui;

import buildcraft.lib.gui.pos.IGuiPosition;

@FunctionalInterface
public interface ISimpleDrawable {
    void drawAt(int x, int y);

    default void drawAt(IGuiPosition element) {
        drawAt(element.getX(), element.getY());
    }
}
