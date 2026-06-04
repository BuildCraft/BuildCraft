/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */
package buildcraft.lib.gui.elem;

import java.util.ArrayList;
import java.util.List;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.client.gui.DrawContext;

import buildcraft.lib.gui.BuildCraftGui;
import buildcraft.lib.gui.IContainingElement;
import buildcraft.lib.gui.IGuiElement;
import buildcraft.lib.gui.pos.IGuiArea;
import buildcraft.lib.gui.pos.IGuiPosition;

@Environment(EnvType.CLIENT)
public class ScrollWindow implements IContainingElement {

    public final BuildCraftGui gui;
    public final IGuiArea area;
    public final List<IGuiElement> innerElements = new ArrayList<>();
    public final IGuiPosition basePosition = new ScrollingElement();

    private int scrollPosition = 0;

    public ScrollWindow(BuildCraftGui gui, IGuiArea area) {
        this.gui = gui;
        this.area = area;
    }

    @Override public double getX() { return area.getX(); }
    @Override public double getY() { return area.getY(); }
    @Override public double getWidth() { return area.getWidth(); }
    @Override public double getHeight() { return area.getHeight(); }

    @Override
    public List<IGuiElement> getChildElements() {
        return innerElements;
    }

    @Override
    public void drawBackground(DrawContext context, float partialTicks) {
        int x1 = (int) area.getX(), y1 = (int) area.getY();
        int x2 = (int) area.getEndX(), y2 = (int) area.getEndY();
        context.enableScissor(x1, y1, x2, y2);
        for (IGuiElement element : innerElements) element.drawBackground(context, partialTicks);
        context.disableScissor();
    }

    @Override
    public void drawForeground(DrawContext context, float partialTicks) {
        int x1 = (int) area.getX(), y1 = (int) area.getY();
        int x2 = (int) area.getEndX(), y2 = (int) area.getEndY();
        context.enableScissor(x1, y1, x2, y2);
        for (IGuiElement element : innerElements) element.drawForeground(context, partialTicks);
        context.disableScissor();
    }

    @Override
    public void addToolTips(List<ToolTip> tooltips) {
        if (area.contains(gui.mouse)) {
            for (IGuiElement element : innerElements) element.addToolTips(tooltips);
        }
    }

    @Override
    public void onMouseClicked(int button) {
        if (area.contains(gui.mouse)) {
            IContainingElement.super.onMouseClicked(button);
        }
    }

    public IGuiPosition calculateNextPosition() {
        if (innerElements.isEmpty()) return basePosition;
        return innerElements.get(innerElements.size() - 1).getPosition(-1, 1);
    }

    public ScrollbarData calculateScrollbarData() {
        double totalHeight = 0;
        for (IGuiElement element : innerElements) totalHeight += element.getHeight();
        return new ScrollbarData(getHeight(), totalHeight, scrollPosition);
    }

    public int getScrollPosition() {
        return scrollPosition;
    }

    public class ScrollbarData {
        public final double shownHeight;
        public final double totalHeight;
        public final double position;

        public ScrollbarData(double shownHeight, double totalHeight, double position) {
            this.shownHeight = shownHeight;
            this.totalHeight = totalHeight;
            this.position = position;
        }

        public void setScrollPosition(double newPosition) {
            int rounded = (int) Math.round(newPosition);
            double maxDist = totalHeight - shownHeight;
            if (maxDist <= 0) { scrollPosition = 0; return; }
            if (rounded + 1 > maxDist) rounded = 1 + (int) maxDist;
            else if (rounded < 0) rounded = 0;
            scrollPosition = rounded;
        }
    }

    private class ScrollingElement implements IGuiPosition {
        @Override public double getX() { return area.getX(); }
        @Override public double getY() { return area.getY() - scrollPosition; }
    }
}
