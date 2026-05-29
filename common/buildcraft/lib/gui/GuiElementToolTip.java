/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */
package buildcraft.lib.gui;

import java.util.List;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import buildcraft.lib.gui.elem.ToolTip;
import buildcraft.lib.gui.pos.IGuiArea;

// STUB(R.Chen): GUI render — Phase 5.
@Environment(EnvType.CLIENT)
public class GuiElementToolTip extends GuiElementSimple {
    public final ITooltipElement source;

    public GuiElementToolTip(BuildCraftGui gui, IGuiArea area, ITooltipElement source) {
        super(gui, area);
        this.source = source;
    }

    @Override
    public void addToolTips(List<ToolTip> tooltips) {
        // STUB(R.Chen): mouse position check deferred — Phase 5
        source.addToolTips(tooltips);
    }
}
