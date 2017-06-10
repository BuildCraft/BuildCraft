/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.gui;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiButton;

import buildcraft.lib.gui.elem.ToolTip;
import buildcraft.lib.misc.GuiUtil;

public class GuiElementToolTips implements IGuiElement {
    private final GuiBC8<?> gui;

    public GuiElementToolTips(GuiBC8<?> gui) {
        this.gui = gui;
    }

    @Override
    public int getX() {
        return gui.mouse.getX();
    }

    @Override
    public int getY() {
        return gui.mouse.getY();
    }

    @Override
    public int getWidth() {
        return 0;
    }

    @Override
    public int getHeight() {
        return 0;
    }

    @Override
    public void drawForeground(float partialTicks) {
        List<ToolTip> tooltips = new ArrayList<>();
        if (gui instanceof ITooltipElement) {
            checkAndAddTooltip(tooltips, (ITooltipElement) gui);
        }
        for (IGuiElement elem : gui.guiElements) {
            checkAndAddTooltip(tooltips, elem);
        }
        checkAndAddTooltip(tooltips, gui.ledgersLeft);
        checkAndAddTooltip(tooltips, gui.ledgersRight);
        for (GuiButton button : gui.getButtonList()) {
            if (button instanceof ITooltipElement) {
                checkAndAddTooltip(tooltips, (ITooltipElement) button);
            }
        }
        GuiUtil.drawVerticallyAppending(this, tooltips, this::drawTooltip);
    }

    private static void checkAndAddTooltip(List<ToolTip> tooltips, ITooltipElement elem) {
        elem.addToolTips(tooltips);
    }

    private int drawTooltip(ToolTip tooltip, int x, int y) {
        return 4 + GuiUtil.drawHoveringText(tooltip, x, y, gui.width, gui.height, -1, gui.mc.fontRenderer);
    }
}
