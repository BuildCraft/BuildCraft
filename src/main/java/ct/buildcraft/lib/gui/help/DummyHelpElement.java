/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.lib.gui.help;

import java.util.List;

import ct.buildcraft.lib.expression.api.IExpressionNode.INodeBoolean;
import ct.buildcraft.lib.expression.node.value.NodeConstantBoolean;
import ct.buildcraft.lib.gui.IGuiElement;
import ct.buildcraft.lib.gui.help.ElementHelpInfo.HelpPosition;
import ct.buildcraft.lib.gui.pos.IGuiArea;

/** A simple, non-drawing, gui element that holds an {@link ElementHelpInfo}. */
public class DummyHelpElement implements IGuiElement {
    public final IGuiArea area;
    public final ElementHelpInfo help;
    private final INodeBoolean visible;

    public DummyHelpElement(IGuiArea area, ElementHelpInfo help) {
        this(area, help, NodeConstantBoolean.TRUE);
    }

    public DummyHelpElement(IGuiArea area, ElementHelpInfo help, INodeBoolean visible) {
        this.area = area;
        this.help = help;
        this.visible = visible;
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
        if (visible.evaluate()) {
            elements.add(help.target(area));
        }
    }
}
