/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */
package buildcraft.lib.gui.elem;

import java.util.List;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.client.gui.DrawContext;

import buildcraft.lib.gui.BuildCraftGui;
import buildcraft.lib.gui.IGuiElement;
import buildcraft.lib.gui.pos.IGuiPosition;

@Environment(EnvType.CLIENT)
public class GuiElementContainerResizing extends GuiElementContainer2 {

    public final IGuiPosition childRoot;
    private double minX, minY;
    private double maxX, maxY;

    public GuiElementContainerResizing(BuildCraftGui gui, IGuiPosition childRoot) {
        super(gui);
        this.childRoot = childRoot;
        minX = maxX = childRoot.getX();
        minY = maxY = childRoot.getY();
    }

    @Override
    public IGuiPosition getChildElementPosition() {
        return childRoot;
    }

    @Override public double getX() { return childRoot.getX() + minX; }
    @Override public double getY() { return childRoot.getY() + minY; }
    @Override public double getWidth() { return maxX - minX; }
    @Override public double getHeight() { return maxY - minY; }

    @Override
    public void calculateSizes() {
        maxX = minX = maxY = minY = 0;
        double x = childRoot.getX(), y = childRoot.getY();
        double x0 = x, x1 = x, y0 = y, y1 = y;
        for (IGuiElement elem : getChildElements()) {
            x0 = Math.min(x0, elem.getX());
            y0 = Math.min(y0, elem.getY());
            x1 = Math.max(x1, elem.getEndX());
            y1 = Math.max(y1, elem.getEndY());
        }
        minX = x0 - x;
        maxX = x1 - x;
        minY = y0 - y;
        maxY = y1 - y;
    }

    @Override
    public void drawBackground(DrawContext context, float partialTicks) {
        for (IGuiElement elem : getChildElements()) elem.drawBackground(context, partialTicks);
    }

    @Override
    public void drawForeground(DrawContext context, float partialTicks) {
        for (IGuiElement elem : getChildElements()) elem.drawForeground(context, partialTicks);
    }

    @Override
    public void addToolTips(List<ToolTip> tooltips) {
        super.addToolTips(tooltips);
    }
}
