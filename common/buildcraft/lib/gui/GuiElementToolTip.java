/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.gui;

import buildcraft.lib.gui.elem.ToolTip;
import buildcraft.lib.gui.pos.IGuiArea;

import java.util.List;

public class GuiElementToolTip extends GuiElementSimple {
    public final ITooltipElement source;

    public GuiElementToolTip(BuildCraftGui gui, IGuiArea area, ITooltipElement source) {
        super(gui, area);
        this.source = source;
    }

    @Override
    public void addToolTips(List<ToolTip> tooltips) {
        if (contains(gui.mouse)) {
            source.addToolTips(tooltips);
        }
    }
}
