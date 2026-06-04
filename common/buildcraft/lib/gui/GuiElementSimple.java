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
import buildcraft.lib.gui.help.ElementHelpInfo.HelpPosition;
import buildcraft.lib.gui.pos.IGuiArea;

@Environment(EnvType.CLIENT)
public class GuiElementSimple implements IGuiElement {
    public final BuildCraftGui gui;
    public String name = null;
    private final IGuiArea element;

    public GuiElementSimple(BuildCraftGui gui, IGuiArea element) {
        this.gui = gui;
        this.element = element;
    }

    @Override public double getX() { return element.getX(); }
    @Override public double getY() { return element.getY(); }
    @Override public double getWidth() { return element.getWidth(); }
    @Override public double getHeight() { return element.getHeight(); }
    @Override public String getDebugInfo(List<String> info) { return name == null ? toString() : name; }
    @Override public void addToolTips(List<ToolTip> tooltips) {}
    @Override public void addHelpElements(List<HelpPosition> elements) {}
}
