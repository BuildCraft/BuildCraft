/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */
package buildcraft.lib.gui.elem;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.client.gui.DrawContext;

import buildcraft.lib.gui.BuildCraftGui;
import buildcraft.lib.gui.IGuiElement;
import buildcraft.lib.gui.pos.IGuiArea;

/** A type of {@link GuiElementContainer2} that restricts the visible size of elements contained within. */
@Environment(EnvType.CLIENT)
public class GuiElementContainerScissor extends GuiElementContainer2 {

    public final IGuiArea area;

    public GuiElementContainerScissor(BuildCraftGui gui, IGuiArea area) {
        super(gui);
        this.area = area;
    }

    @Override public double getX() { return area.getX(); }
    @Override public double getY() { return area.getY(); }
    @Override public double getWidth() { return area.getWidth(); }
    @Override public double getHeight() { return area.getHeight(); }

    @Override
    public void drawBackground(DrawContext context, float partialTicks) {
        int x1 = (int) area.getX();
        int y1 = (int) area.getY();
        int x2 = (int) area.getEndX();
        int y2 = (int) area.getEndY();
        context.enableScissor(x1, y1, x2, y2);
        for (IGuiElement elem : getChildElements()) elem.drawBackground(context, partialTicks);
        context.disableScissor();
    }

    @Override
    public void drawForeground(DrawContext context, float partialTicks) {
        int x1 = (int) area.getX();
        int y1 = (int) area.getY();
        int x2 = (int) area.getEndX();
        int y2 = (int) area.getEndY();
        context.enableScissor(x1, y1, x2, y2);
        for (IGuiElement elem : getChildElements()) elem.drawForeground(context, partialTicks);
        context.disableScissor();
    }
}
