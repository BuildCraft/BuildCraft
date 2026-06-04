/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */
package buildcraft.lib.gui.elem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.client.gui.DrawContext;

import buildcraft.lib.gui.BuildCraftGui;
import buildcraft.lib.gui.IGuiElement;
import buildcraft.lib.gui.pos.IGuiPosition;

/** @deprecated Help should be moved to GuiElementContainer rather than this. */
@Deprecated
@Environment(EnvType.CLIENT)
public class GuiElementContainerHelp implements IGuiElement {
    public final BuildCraftGui gui;
    private final IGuiPosition position;
    private final List<IGuiElement> internalElements = new ArrayList<>();
    private double width, height;
    private boolean calc = false;

    public GuiElementContainerHelp(BuildCraftGui gui, IGuiPosition position) {
        this.gui = gui;
        this.position = position;
    }

    private void recalcSize() {
        calc = true;
        double w = 0, h = 0;
        for (IGuiElement element : internalElements) {
            w = Math.max(w, element.getEndX());
            h = Math.max(h, element.getEndY());
        }
        width = w;
        height = h;
        calc = false;
    }

    public void add(IGuiElement element) {
        internalElements.add(element);
        recalcSize();
    }

    public void addAll(IGuiElement... elements) {
        Collections.addAll(internalElements, elements);
        recalcSize();
    }

    public void addAll(Collection<IGuiElement> elements) {
        internalElements.addAll(elements);
        recalcSize();
    }

    @Override public double getX() { return calc ? 0 : position.getX(); }
    @Override public double getY() { return calc ? 0 : position.getY(); }
    @Override public double getWidth() { return width; }
    @Override public double getHeight() { return height; }

    @Override
    public void drawBackground(DrawContext context, float partialTicks) {
        for (IGuiElement element : internalElements) element.drawBackground(context, partialTicks);
    }

    @Override
    public void drawForeground(DrawContext context, float partialTicks) {
        for (IGuiElement element : internalElements) element.drawForeground(context, partialTicks);
    }

    @Override
    public void addToolTips(List<ToolTip> tooltips) {
        for (IGuiElement element : internalElements) element.addToolTips(tooltips);
    }

    @Override
    public void addHelpElements(List<buildcraft.lib.gui.help.ElementHelpInfo.HelpPosition> elements) {
        for (IGuiElement element : internalElements) element.addHelpElements(elements);
    }
}
