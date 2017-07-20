/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.gui.elem;

import buildcraft.lib.gui.GuiBC8;
import buildcraft.lib.gui.IGuiElement;
import buildcraft.lib.gui.pos.IGuiPosition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class GuiElementContainer implements IGuiElement {
    public final GuiBC8<?> gui;
    private final IGuiPosition position;
    private final List<IGuiElement> internalElements = new ArrayList<>();
    private int width, height;
    private boolean calc = false;

    public GuiElementContainer(GuiBC8<?> gui, IGuiPosition position) {
        this.gui = gui;
        this.position = position;
    }

    private void recalcSize() {
        calc = true;
        width = 0;
        height = 0;
        int w = 0;
        int h = 0;
        for (IGuiElement element : internalElements) {
            w = Math.max(w, element.getEndX());
            h = Math.max(h, element.getEndY());
        }
        width = w;
        height = h;
        calc = false;
    }

    /** Adds the given element to be drawn. Note that the element must be based around (0, 0), NOT this element. */
    public void add(IGuiElement element) {
        internalElements.add(element);
        recalcSize();
    }

    /** Adds all of the given elements, like in {@link #add(IGuiElement)} */
    public void addAll(IGuiElement... elements) {
        Collections.addAll(internalElements, elements);
        recalcSize();
    }

    /** Adds all of the given elements, like in {@link #add(IGuiElement)} */
    public void addAll(Collection<IGuiElement> elements) {
        internalElements.addAll(elements);
        recalcSize();
    }

    @Override
    public int getX() {
        return calc ? 0 : position.getX();
    }

    @Override
    public int getY() {
        return calc ? 0 : position.getY();
    }

    @Override
    public int getWidth() {
        return this.width;
    }

    @Override
    public int getHeight() {
        return this.height;
    }

    @Override
    public void drawBackground(float partialTicks) {
        for (IGuiElement element : internalElements) {
            element.drawBackground(partialTicks);
        }
    }

    @Override
    public void drawForeground(float partialTicks) {
        for (IGuiElement element : internalElements) {
            element.drawForeground(partialTicks);
        }
    }

    @Override
    public void addToolTips(List<ToolTip> tooltips) {
        for (IGuiElement element : internalElements) {
            element.addToolTips(tooltips);
        }
    }
}
