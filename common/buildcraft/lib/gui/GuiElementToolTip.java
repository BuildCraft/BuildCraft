/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.gui;

import java.util.List;

import buildcraft.lib.gui.elem.ToolTip;
import buildcraft.lib.gui.pos.GuiRectangle;
import buildcraft.lib.gui.pos.IGuiArea;
import buildcraft.lib.gui.pos.IGuiPosition;

public class GuiElementToolTip extends GuiElementSimple<GuiBC8<?>> {
    public final ITooltipElement source;

    public GuiElementToolTip(GuiBC8<?> gui, IGuiArea element, ITooltipElement source) {
        super(gui, element);
        this.source = source;
    }

    public GuiElementToolTip(GuiBC8<?> gui, IGuiPosition parent, GuiRectangle rectangle, ITooltipElement source) {
        super(gui, parent, rectangle);
        this.source = source;
    }

    @Override
    public void addToolTips(List<ToolTip> tooltips) {
        if (contains(gui.mouse)) {
            source.addToolTips(tooltips);
        }
    }
}
