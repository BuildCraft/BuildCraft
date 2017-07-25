/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.gui;

import buildcraft.lib.gui.pos.GuiRectangle;
import buildcraft.lib.gui.pos.IGuiArea;
import buildcraft.lib.gui.pos.IGuiPosition;

public class GuiElementSimple<G extends GuiBC8<?>> implements IGuiElement {
    public final G gui;
    private final IGuiArea element;

    public GuiElementSimple(G gui, IGuiArea element) {
        this.gui = gui;
        this.element = element;
    }

    @Override
    public int getX() {
        return element.getX();
    }

    @Override
    public int getY() {
        return element.getY();
    }

    @Override
    public int getWidth() {
        return element.getWidth();
    }

    @Override
    public int getHeight() {
        return element.getHeight();
    }
}
