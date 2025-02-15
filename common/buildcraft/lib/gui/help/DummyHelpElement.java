/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.gui.help;

import buildcraft.lib.gui.IGuiElement;
import buildcraft.lib.gui.help.ElementHelpInfo.HelpPosition;
import buildcraft.lib.gui.pos.IGuiArea;

import java.util.List;

/** A simple, non-drawing, gui element that holds an {@link ElementHelpInfo}. */
public class DummyHelpElement implements IGuiElement {
    public final IGuiArea area;
    public final ElementHelpInfo help;

    public DummyHelpElement(IGuiArea area, ElementHelpInfo help) {
        this.area = area;
        this.help = help;
    }

    @Override
    public double getX() {
        return area.getX();
    }

    @Override
    public double getY() {
        return area.getY();
    }

    @Override
    public double getWidth() {
        return area.getWidth();
    }

    @Override
    public double getHeight() {
        return area.getHeight();
    }

    @Override
    public void addHelpElements(List<HelpPosition> elements) {
        elements.add(help.target(area));
    }
}
